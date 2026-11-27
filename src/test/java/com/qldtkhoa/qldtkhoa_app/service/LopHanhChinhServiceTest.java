package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.CoVanClassRosterView;
import com.qldtkhoa.qldtkhoa_app.dto.HocLucSummary;
import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhForm;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.GiangVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LopHanhChinhServiceTest {

    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;
    @Mock
    private KhoaNhapHocRepository khoaNhapHocRepository;
    @Mock
    private NganhHocRepository nganhHocRepository;
    @Mock
    private GiangVienRepository giangVienRepository;
    @Mock
    private SinhVienRepository sinhVienRepository;
    @Mock
    private SinhVienService sinhVienService;
    @Mock
    private BangDiemSinhVienViewRepository bangDiemViewRepository;
    @Mock
    private HocLucService hocLucService;

    @InjectMocks
    private LopHanhChinhService service;

    @Test
    @DisplayName("create: Trùng mã lớp hành chính hoặc thiếu cố vấn ném exception")
    void create_duplicateOrMissingAdvisor_throws() {
        LopHanhChinhForm dup = new LopHanhChinhForm();
        dup.setMaLopHC("CNTT1");
        when(lopHanhChinhRepository.existsById("CNTT1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(dup)).isInstanceOf(DuplicateResourceException.class);

        LopHanhChinhForm noAdvisor = new LopHanhChinhForm();
        noAdvisor.setMaLopHC("CNTT2");
        noAdvisor.setMaKhoaHoc("K66");
        noAdvisor.setMaNganh("CNTT");
        when(lopHanhChinhRepository.existsById("CNTT2")).thenReturn(false);
        when(khoaNhapHocRepository.findById("K66")).thenReturn(Optional.of(mock(KhoaNhapHoc.class)));
        when(nganhHocRepository.findById("CNTT")).thenReturn(Optional.of(mock(NganhHoc.class)));

        assertThatThrownBy(() -> service.create(noAdvisor))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("lophc.covan.required");
    }

    @Test
    @DisplayName("delete: Chặn xóa khi đã có sinh viên, xóa sạch khi lớp trống")
    void delete_guardsNonEmptyClass() {
        when(lopHanhChinhRepository.findById("L1")).thenReturn(Optional.of(mock(LopHanhChinh.class)));
        when(sinhVienRepository.existsByLopHanhChinh_MaLopHC("L1")).thenReturn(true);

        assertThatThrownBy(() -> service.delete("L1")).isInstanceOf(BusinessRuleException.class);

        when(sinhVienRepository.existsByLopHanhChinh_MaLopHC("L1")).thenReturn(false);
        service.delete("L1");
        verify(lopHanhChinhRepository).deleteById("L1");
    }

    @Test
    @DisplayName("getAdvisedClassRoster: Chặn giảng viên không phải cố vấn hoặc lớp chưa có cố vấn")
    void getAdvisedClassRoster_unauthorized_throwsAccessDenied() {
        LopHanhChinh lhc = mock(LopHanhChinh.class);
        when(lhc.getCoVan()).thenReturn(null);
        when(lopHanhChinhRepository.findByIdWithDetails("L1")).thenReturn(Optional.of(lhc));

        assertThatThrownBy(() -> service.getAdvisedClassRoster("L1", "GV01", false))
                .isInstanceOf(AccessDeniedException.class);

        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV_OTHER");
        when(lhc.getCoVan()).thenReturn(gv);

        assertThatThrownBy(() -> service.getAdvisedClassRoster("L1", "GV01", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getAdvisedClassRoster: Lọc đúng sinh viên có nguy cơ học vụ khi onlyAtRisk = true")
    void getAdvisedClassRoster_filtersAtRiskStudents() {
        NhanSu ns = mock(NhanSu.class);
        when(ns.getHoTen()).thenReturn("TS. A");
        GiangVien gv = mock(GiangVien.class);
        when(gv.getMaNS()).thenReturn("GV01");
        when(gv.getNhanSu()).thenReturn(ns);

        LopHanhChinh lhc = mock(LopHanhChinh.class);
        when(lhc.getMaLopHC()).thenReturn("L1");
        when(lhc.getTenLopHC()).thenReturn("CNTT A");
        when(lhc.getCoVan()).thenReturn(gv);
        when(lopHanhChinhRepository.findByIdWithDetails("L1")).thenReturn(Optional.of(lhc));

        SinhVienRow svOk = new SinhVienRow("SV1", "An", "sv1@edu.vn", GioiTinh.NAM, LocalDate.of(2005, 1, 1), "L1", "CNTT A", TrangThaiSinhVien.DANG_HOC);
        SinhVienRow svRisk = new SinhVienRow("SV2", "Bình", "sv2@edu.vn", GioiTinh.NAM, LocalDate.of(2005, 2, 2), "L1", "CNTT A", TrangThaiSinhVien.DANG_HOC);
        when(sinhVienService.findByLopHanhChinh("L1")).thenReturn(List.of(svOk, svRisk));
        when(bangDiemViewRepository.findById_MaSVInOrderByNamHocDescMaHKAscTenMonAsc(List.of("SV1", "SV2")))
                .thenReturn(List.of());
        when(hocLucService.evaluateSummary(any()))
                .thenReturn(new HocLucSummary(30, 3.2, "Giỏi", 0, false, false, List.of(), java.util.Map.of()))
                .thenReturn(new HocLucSummary(15, 1.5, "Yếu", 1, true, true, List.of("CPA < 2.0"), java.util.Map.of()));

        CoVanClassRosterView view = service.getAdvisedClassRoster("L1", "GV01", true);

        assertThat(view.totalStudents()).isEqualTo(2);
        assertThat(view.atRiskCount()).isEqualTo(1);
        assertThat(view.studentRows()).hasSize(1);
        assertThat(view.studentRows().get(0).maSV()).isEqualTo("SV2");
    }
}
