package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.MonHocForm;
import com.qldtkhoa.qldtkhoa_app.entity.DieuKienMonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DieuKienMonHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

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

    @InjectMocks
    private MonHocService monHocService;

    @Test
    @DisplayName("create: Tổng trọng số QT + GK + CK khác 1.0 sẽ ném BusinessRuleException")
    void create_invalidWeightSum_throwsBusinessRuleException() {
        MonHocForm form = new MonHocForm();
        form.setMaMon("INT1001");
        form.setTenMon("Tin học đại cương");
        form.setSoTinChi(3);
        form.setTinChiLT(2);
        form.setTinChiTH(1);
        form.setTrongSoQT(0.2);
        form.setTrongSoGK(0.2);
        form.setTrongSoCK(0.5); // Sum = 0.9 != 1.0
        form.setMaBoMon("BM_CNPM");

        assertThatThrownBy(() -> monHocService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Tổng trọng số đánh giá");
    }

    @Test
    @DisplayName("create: Tín chỉ LT + TH khác tổng số tín chỉ sẽ ném BusinessRuleException")
    void create_invalidCreditsSum_throwsBusinessRuleException() {
        MonHocForm form = new MonHocForm();
        form.setMaMon("INT1001");
        form.setTenMon("Tin học đại cương");
        form.setSoTinChi(4); // 4
        form.setTinChiLT(2);
        form.setTinChiTH(1); // 2 + 1 = 3 != 4
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

        // Giả lập đã có phụ thuộc từ MON_B -> MON_A (B cần A)
        DieuKienMonHoc dkEdge = mock(DieuKienMonHoc.class);
        when(dkEdge.getMonTienQuyet()).thenReturn(mA);
        when(dieuKienMonHocRepository.findByMonHoc_MaMon("MON_B")).thenReturn(List.of(dkEdge));
        when(mA.getMaMon()).thenReturn("MON_A");

        // Giờ lại thêm MON_A phụ thuộc MON_B -> Tạo chu trình A -> B -> A
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
        org.assertj.core.api.Assertions.assertThat(result).isSameAs(mh);
    }
}
