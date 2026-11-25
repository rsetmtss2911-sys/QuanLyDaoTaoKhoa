package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.AdminDashboardView;
import com.qldtkhoa.qldtkhoa_app.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'PHONG_DAO_TAO')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(@RequestParam(required = false) String maHK, Model model) {
        AdminDashboardView dashboard = dashboardService.getAdminDashboard(maHK);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("currentPath", "/admin/dashboard");
        return "admin/dashboard";
    }
}
