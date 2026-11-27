package com.qldtkhoa.qldtkhoa_app.controller;

import com.qldtkhoa.qldtkhoa_app.dto.ChangePasswordForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.service.TaiKhoanService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final TaiKhoanService taiKhoanService;
    private final Environment environment;

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/";
        }
        boolean showDemo = environment != null && (
                environment.acceptsProfiles(Profiles.of("dev", "local"))
                || Boolean.TRUE.equals(environment.getProperty("app.ui.show-demo-credentials", Boolean.class, false))
        );
        model.addAttribute("showDemoCredentials", showDemo);
        return "auth/login";
    }

    @GetMapping("/doi-mat-khau")
    public String changePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "auth/change-password";
    }

    @PostMapping("/doi-mat-khau")
    public String changePassword(@Valid @ModelAttribute("form") ChangePasswordForm form,
                                 BindingResult result,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/change-password";
        }
        try {
            HttpSession session = request != null ? request.getSession(false) : null;
            String currentSessionId = session != null ? session.getId() : null;
            taiKhoanService.changePassword(authentication.getName(), form, currentSessionId);
            redirect.addFlashAttribute("flash", FlashMessage.success("auth.password.changed"));
            return "redirect:/";
        } catch (BusinessRuleException ex) {
            result.reject(ex.getMessageKey(), ex.getMessage() != null ? ex.getMessage() : "Mật khẩu không hợp lệ.");
            return "auth/change-password";
        }
    }
}
