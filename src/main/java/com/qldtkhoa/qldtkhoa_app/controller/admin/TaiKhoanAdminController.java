package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.TaiKhoanService;
import com.qldtkhoa.qldtkhoa_app.service.VaiTroService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tai-khoan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TaiKhoanAdminController {

    private final TaiKhoanService taiKhoanService;
    private final VaiTroService vaiTroService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maVaiTro,
                       @PageableDefault(size = 20, sort = "email") Pageable pageable,
                       Model model) {
        model.addAttribute("page", taiKhoanService.searchRows(q, maVaiTro, SortWhitelist.of(pageable, "email")));
        model.addAttribute("vaiTros", vaiTroService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("maVaiTro", maVaiTro);
        return "admin/tai-khoan/list";
    }

    @PostMapping("/toggle-lock")
    public String toggleLock(@RequestParam String email,
                             @AuthenticationPrincipal AppUserPrincipal principal,
                             RedirectAttributes redirect) {
        try {
            String currentAdminEmail = principal != null ? principal.getEmail() : null;
            boolean locked = taiKhoanService.toggleLock(email, currentAdminEmail);
            String key = locked ? "taikhoan.locked" : "taikhoan.unlocked";
            redirect.addFlashAttribute("flash", FlashMessage.success(key, email));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/tai-khoan";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, RedirectAttributes redirect) {
        try {
            taiKhoanService.resetPassword(email, "123456");
            redirect.addFlashAttribute("flash", FlashMessage.success("taikhoan.resetPassword.success", email));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/tai-khoan";
    }
}
