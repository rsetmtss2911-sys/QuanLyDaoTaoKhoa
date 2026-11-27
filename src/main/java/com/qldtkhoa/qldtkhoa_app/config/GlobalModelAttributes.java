package com.qldtkhoa.qldtkhoa_app.config;

import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final AcademicPolicy academicPolicy;

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("academicPolicy")
    public AcademicPolicy academicPolicy() {
        return academicPolicy;
    }

    @ModelAttribute("currentUserDisplayName")
    public String currentUserDisplayName(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.getDisplayName();
        }
        return authentication.getName();
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.getUsername();
        }
        return authentication.getName();
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(Authentication authentication) {
        if (authentication == null) {
            return "Vai trò";
        }
        if (authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.getRole();
        }
        return "Vai trò";
    }
}
