package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.CoVanClassRosterView;
import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.ChuongTrinhDaoTaoService;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.HocLucService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiangVienCoVanControllerTest {

    @Mock
    private LopHanhChinhService lopHanhChinhService;
    @Mock
    private SinhVienService sinhVienService;
    @Mock
    private HocLucService hocLucService;
    @Mock
    private ChuongTrinhDaoTaoService ctdtService;
    @Mock
    private QuyetDinhHocVuService quyetDinhHocVuService;
    @Mock
    private DangKyHocPhanService dangKyHocPhanService;

    @InjectMocks
    private GiangVienCoVanController controller;

    @Test
    @DisplayName("classRoster should delegate to getAdvisedClassRoster and populate model attributes")
    void testClassRoster_assignedAdvisor_success() {
        String maLop = "CNTT-K24A";
        LopHanhChinhDetailView lop = new LopHanhChinhDetailView(
                maLop, "CNTT K24 A", "K2024", "CNTT", "GV_01", "TS. Nguyễn Văn A");
        List<SinhVienRow> mockStudents = Collections.emptyList();
        CoVanClassRosterView rosterView = new CoVanClassRosterView(lop, mockStudents, List.of(), 0L, 0);
        when(lopHanhChinhService.getAdvisedClassRoster(maLop, "GV_01", false)).thenReturn(rosterView);

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_01");
        Model model = new ConcurrentModel();

        String view = controller.classRoster(maLop, principal, model);

        assertThat(view).isEqualTo("giangvien/co-van/detail");
        assertThat(model.getAttribute("lop")).isSameAs(lop);
        assertThat(model.getAttribute("students")).isSameAs(mockStudents);
        assertThat(model.getAttribute("onlyAtRisk")).isEqualTo(false);
        verify(lopHanhChinhService).getAdvisedClassRoster(maLop, "GV_01", false);
    }

    @Test
    @DisplayName("classRoster should propagate AccessDeniedException when lecturer is not advisor")
    void testClassRoster_unassignedLecturer_throwsAccessDenied() {
        String maLop = "CNTT-K24A";
        when(lopHanhChinhService.getAdvisedClassRoster(maLop, "GV_02", false))
                .thenThrow(new AccessDeniedException("Bạn không phải là Cố vấn học tập của lớp hành chính này!"));

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_02");
        Model model = new ConcurrentModel();

        assertThatThrownBy(() -> controller.classRoster(maLop, principal, model))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Bạn không phải là Cố vấn học tập");
    }

    @Test
    @DisplayName("studentDetail should propagate AccessDeniedException for non-advisor lecturer")
    void testStudentDetail_unassignedAdvisor_throwsAccessDenied() {
        when(sinhVienService.getAdvisedStudentDetail("SV001", "GV_02"))
                .thenThrow(new AccessDeniedException("Bạn không phải là Cố vấn học tập của sinh viên này!"));
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_02");

        assertThatThrownBy(() -> controller.studentDetail("SV001", principal, new ConcurrentModel()))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(hocLucService, ctdtService, quyetDinhHocVuService, dangKyHocPhanService);
    }

    @Test
    @DisplayName("studentDetail should populate student detail DTO, transcript and curriculum progress")
    void testStudentDetail_assignedAdvisor_success() {
        SinhVienDetailView sv = new SinhVienDetailView(
                "SV001", "Trần Thị Mai", GioiTinh.NU, LocalDate.of(2005, 8, 15),
                TrangThaiSinhVien.DANG_HOC, "CNTT2023A", "CNTT", "K2023", 2023,
                "GV_01", "TS. A", "sv001@vnu.edu.vn", true);
        when(sinhVienService.getAdvisedStudentDetail("SV001", "GV_01")).thenReturn(sv);
        when(hocLucService.evaluateStudent("SV001")).thenReturn(
                new BangDiemSinhVienResponse(Collections.emptyMap(), 0, null, "—"));
        when(ctdtService.getTienDoCTDT("SV001")).thenReturn(
                new TienDoHocTapView("K2023", "CNTT", "CNTT", "CNTT2023A", 0, 130, 0, List.of(), java.util.Set.of()));
        when(quyetDinhHocVuService.findBySinhVien("SV001")).thenReturn(List.of());
        when(dangKyHocPhanService.findBySinhVien("SV001")).thenReturn(List.of());

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_01");
        Model model = new ConcurrentModel();

        String view = controller.studentDetail("SV001", principal, model);

        assertThat(view).isEqualTo("giangvien/co-van/sinh-vien-detail");
        assertThat(model.getAttribute("sv")).isSameAs(sv);
    }
}
