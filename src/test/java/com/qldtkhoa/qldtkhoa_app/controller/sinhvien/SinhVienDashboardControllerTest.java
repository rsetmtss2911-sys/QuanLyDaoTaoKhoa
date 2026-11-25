package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.DangKyDaChonRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDashboardView;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinhVienDashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private SinhVienDashboardController controller;

    @Test
    @DisplayName("dashboard should populate model with dashboard data and return sinhvien/dashboard")
    void dashboard_withRegistrations_returnsDashboardViewAndModel() {
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaSV()).thenReturn("SV001");

        DangKyDaChonRow courseRow = new DangKyDaChonRow(
                "LHP01",
                "INT2204",
                "Lập trình hướng đối tượng",
                3,
                "TS. Nguyễn Văn A",
                "T2 (1-3) 301-G2",
                LocalDateTime.now(),
                TrangThaiDangKy.HOP_LE
        );

        // Verify both accessor methods work identically
        assertThat(courseRow.tenGV()).isEqualTo("TS. Nguyễn Văn A");
        assertThat(courseRow.tenGiangVien()).isEqualTo("TS. Nguyễn Văn A");

        TimetableSlot slot = new TimetableSlot(
                2, 1, 3, "Lập trình hướng đối tượng", "LHP01", "301-G2", "TS. Nguyễn Văn A"
        );

        SinhVienDashboardView dashboardView = new SinhVienDashboardView(
                "SV001",
                "Trần Thị Mai",
                "mai.tt@qldt.edu.vn",
                "CNTT2023A",
                "Công nghệ thông tin A",
                "Công nghệ thông tin",
                "K2023",
                TrangThaiSinhVien.DANG_HOC,
                "ThS. Trần B",
                "tranb@qldt.edu.vn",
                "HK1_2025",
                "Học kỳ 1 (2025-2026)",
                Collections.emptyList(),
                18,
                14,
                25,
                64,
                3.45,
                "Giỏi",
                List.of(courseRow),
                List.of(slot),
                Collections.emptyList()
        );

        when(dashboardService.getSinhVienDashboard("SV001", "HK1_2025")).thenReturn(dashboardView);

        Model model = new ConcurrentModel();
        String view = controller.dashboard("HK1_2025", principal, model);

        assertThat(view).isEqualTo("sinhvien/dashboard");
        assertThat(model.getAttribute("dashboard")).isEqualTo(dashboardView);
        assertThat(model.getAttribute("currentPath")).isEqualTo("/sinh-vien/dashboard");
        verify(dashboardService, times(1)).getSinhVienDashboard("SV001", "HK1_2025");
    }

    @Test
    @DisplayName("dashboard without term parameter passes null to dashboardService")
    void dashboard_withoutTermParam_callsServiceWithNull() {
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaSV()).thenReturn("SV001");

        SinhVienDashboardView dashboardView = mock(SinhVienDashboardView.class);
        when(dashboardService.getSinhVienDashboard("SV001", null)).thenReturn(dashboardView);

        Model model = new ConcurrentModel();
        String view = controller.dashboard(null, principal, model);

        assertThat(view).isEqualTo("sinhvien/dashboard");
        assertThat(model.getAttribute("dashboard")).isEqualTo(dashboardView);
        verify(dashboardService, times(1)).getSinhVienDashboard("SV001", null);
    }
}
