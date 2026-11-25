package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.GiangVienDashboardView;
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
@RequestMapping("/giang-vien/dashboard")
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienDashboardController {

    private final DashboardService dashboardService;

    public GiangVienDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String maHK,
                            @AuthenticationPrincipal AppUserPrincipal principal,
                            Model model) {
        GiangVienDashboardView dashboard = dashboardService.getGiangVienDashboard(principal.getMaNS(), maHK);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("currentPath", "/giang-vien/dashboard");
        return "giangvien/dashboard";
    }
}
