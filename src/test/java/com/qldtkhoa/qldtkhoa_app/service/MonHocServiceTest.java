package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.MonHocForm;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.DieuKienMonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DieuKienMonHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KetQuaHocTapRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonHocServiceTest {

    @Mock
    private MonHocRepository monHocRepository;

    @Mock
    private BoMonRepository boMonRepository;

    @Mock
    private DieuKienMonHocRepository dieuKienMonHocRepository;

    @Mock
    private ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @Mock
    private KetQuaHocTapRepository ketQuaHocTapRepository;

    @InjectMocks
    private MonHocService monHocService;

    @Test
    @DisplayName("create: Tổng trọng số QT + GK + CK sai lệch >= 0.0001 sẽ ném BusinessRuleException")
    void create_invalidWeightSum_throwsBusinessRuleException() {
        MonHocForm form = new MonHocForm();
        form.setMaMon("INT1001");
        form.setTenMon("Tin học đại cương");
        form.setSoTinChi(3);
        form.setTinChiLT(2);
        form.setTinChiTH(1);
        form.setTrongSoQT(0.2);
        form.setTrongSoGK(0.3);
        form.setTrongSoCK(0.501); // Sum = 1.001, lệch > 0.0001
        form.setMaBoMon("BM_CNPM");

        assertThatThrownBy(() -> monHocService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tổng trọng số đánh giá");
    }

    @Test
    @DisplayName("update: Môn học đã có điểm thì không được phép thay đổi trọng số hoặc số tín chỉ")
    void update_gradedCourseWeightChanged_throwsBusinessRuleException() {
        BoMon bm = mock(BoMon.class);
        MonHoc existing = new MonHoc("THDC", "Tin học đại cương", 2, 1, 1, 0.2, 0.3, 0.5, bm);
        when(monHocRepository.findById("THDC")).thenReturn(Optional.of(existing));
        when(ketQuaHocTapRepository.existsByDangKy_LopHocPhan_MonHoc_MaMonAndDiemTongIsNotNull("THDC"))
                .thenReturn(true);

        MonHocForm form = new MonHocForm();
        form.setMaMon("THDC");
        form.setTenMon("Tin học đại cương");
        form.setSoTinChi(2);
        form.setTinChiLT(1);
        form.setTinChiTH(1);
        form.setTrongSoQT(0.3);
        form.setTrongSoGK(0.2);
        form.setTrongSoCK(0.5);
        form.setMaBoMon("BM_DC");

        assertThatThrownBy(() -> monHocService.update("THDC", form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("monhoc.trongSoLocked");
    }

    @Test
    @DisplayName("create: Tín chỉ LT + TH khác tổng số tín chỉ sẽ ném BusinessRuleException")
    void create_invalidCreditsSum_throwsBusinessRuleException() {
        MonHocForm form = new MonHocForm();
        form.setMaMon("INT1001");
        form.setTenMon("Tin học đại cương");
        form.setSoTinChi(4);
        form.setTinChiLT(2);
        form.setTinChiTH(1);
        form.setTrongSoQT(0.2);
        form.setTrongSoGK(0.3);
        form.setTrongSoCK(0.5);
        form.setMaBoMon("BM_CNPM");

        assertThatThrownBy(() -> monHocService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tổng số tín chỉ lý thuyết");
    }

    @Test
    @DisplayName("addPrerequisite: Phát hiện chu trình phụ thuộc môn học qua thuật toán BFS")
    void addPrerequisite_cycleDetected_throwsBusinessRuleException() {
        MonHoc mA = mock(MonHoc.class);
        MonHoc mB = mock(MonHoc.class);
        when(monHocRepository.findById("MON_A")).thenReturn(Optional.of(mA));
        when(monHocRepository.findById("MON_B")).thenReturn(Optional.of(mB));
        when(dieuKienMonHocRepository.existsByMonHoc_MaMonAndMonTienQuyet_MaMon("MON_A", "MON_B")).thenReturn(false);

        DieuKienMonHoc dkEdge = mock(DieuKienMonHoc.class);
        when(dkEdge.getMonTienQuyet()).thenReturn(mA);
        when(dieuKienMonHocRepository.findByMonHoc_MaMon("MON_B")).thenReturn(List.of(dkEdge));
        when(mA.getMaMon()).thenReturn("MON_A");

        assertThatThrownBy(() -> monHocService.addPrerequisite("MON_A", "MON_B"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tạo chu trình phụ thuộc");
    }

    @Test
    @DisplayName("getWithDetailsOrThrow: Trả về MonHoc khi tồn tại hoặc ném ResourceNotFoundException")
    void getWithDetailsOrThrow_found_returnsMonHoc() {
        MonHoc mh = mock(MonHoc.class);
        when(monHocRepository.findByIdWithDetails("CTDL")).thenReturn(Optional.of(mh));

        MonHoc result = monHocService.getWithDetailsOrThrow("CTDL");
        assertThat(result).isSameAs(mh);
    }
}
