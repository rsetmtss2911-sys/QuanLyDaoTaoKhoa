package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiangVienCoVanControllerTest {

    @Mock
    private LopHanhChinhService lopHanhChinhService;

    @Mock
    private SinhVienService sinhVienService;

    @InjectMocks
    private GiangVienCoVanController controller;

    @Test
    @DisplayName("classRoster should fetch lop with details and students roster when lecturer is advisor")
    void testClassRoster_assignedAdvisor_success() {
        String maLop = "CNTT-K24A";
        LopHanhChinh mockLop = mock(LopHanhChinh.class);
        com.qldtkhoa.qldtkhoa_app.entity.GiangVien mockGv = mock(com.qldtkhoa.qldtkhoa_app.entity.GiangVien.class);
        when(mockGv.getMaNS()).thenReturn("GV_01");
        when(mockLop.getCoVan()).thenReturn(mockGv);
        when(lopHanhChinhService.getWithDetailsOrThrow(maLop)).thenReturn(mockLop);

        List<SinhVienRow> mockStudents = Collections.emptyList();
        when(sinhVienService.findByLopHanhChinh(maLop)).thenReturn(mockStudents);

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_01");
        Model model = new ConcurrentModel();

        String view = controller.classRoster(maLop, principal, model);

        assertThat(view).isEqualTo("giangvien/co-van/detail");
        assertThat(model.getAttribute("lop")).isSameAs(mockLop);
        assertThat(model.getAttribute("students")).isSameAs(mockStudents);
        verify(lopHanhChinhService).getWithDetailsOrThrow(maLop);
        verify(sinhVienService).findByLopHanhChinh(maLop);
    }

    @Test
    @DisplayName("classRoster should throw AccessDeniedException when lecturer is not advisor")
    void testClassRoster_unassignedLecturer_throwsAccessDenied() {
        String maLop = "CNTT-K24A";
        LopHanhChinh mockLop = mock(LopHanhChinh.class);
        com.qldtkhoa.qldtkhoa_app.entity.GiangVien mockGv = mock(com.qldtkhoa.qldtkhoa_app.entity.GiangVien.class);
        when(mockGv.getMaNS()).thenReturn("GV_01");
        when(mockLop.getCoVan()).thenReturn(mockGv);
        when(lopHanhChinhService.getWithDetailsOrThrow(maLop)).thenReturn(mockLop);

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV_02"); // Different lecturer
        Model model = new ConcurrentModel();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.classRoster(maLop, principal, model))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Bạn không phải là Cố vấn học tập");
    }

    @Test
    @DisplayName("classRoster should throw AccessDeniedException when class has no advisor assigned")
    void testClassRoster_nullAdvisor_throwsAccessDenied() {
        String maLop = "CNTT-K24B";
        LopHanhChinh mockLop = mock(LopHanhChinh.class);
        when(mockLop.getCoVan()).thenReturn(null);
        when(lopHanhChinhService.getWithDetailsOrThrow(maLop)).thenReturn(mockLop);

        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        Model model = new ConcurrentModel();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.classRoster(maLop, principal, model))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Bạn không phải là Cố vấn học tập");
    }
}

