package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDashboardView;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sinh-vien/dashboard")
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienDashboardController {

    private final DashboardService dashboardService;

    public SinhVienDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String maHK,
                            @AuthenticationPrincipal AppUserPrincipal principal,
                            Model model) {
        SinhVienDashboardView dashboard = dashboardService.getSinhVienDashboard(principal.getMaSV(), maHK);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("currentPath", "/sinh-vien/dashboard");
        return "sinhvien/dashboard";
    }
}
