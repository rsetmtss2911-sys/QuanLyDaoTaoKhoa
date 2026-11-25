package com.qldtkhoa.qldtkhoa_app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            if ("ROLE_ADMIN".equals(role) || "ROLE_PHONG_DAO_TAO".equals(role)) {
                return "redirect:/admin/dashboard";
            } else if ("ROLE_GIANG_VIEN".equals(role)) {
                return "redirect:/giang-vien/dashboard";
            } else if ("ROLE_SINH_VIEN".equals(role)) {
                return "redirect:/sinh-vien/dashboard";
            }
        }

        return "redirect:/login";
    }
}
