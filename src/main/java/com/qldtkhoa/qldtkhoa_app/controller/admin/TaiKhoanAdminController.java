package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.service.TaiKhoanService;
import com.qldtkhoa.qldtkhoa_app.service.VaiTroService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/{email}/toggle-lock")
    public String toggleLock(@PathVariable String email, RedirectAttributes redirect) {
        TaiKhoan tk = taiKhoanService.getByEmail(email);
        boolean currentlyActive = Boolean.TRUE.equals(tk.getTrangThaiKichHoat());
        taiKhoanService.setLockStatus(email, currentlyActive); // if currentlyActive, lock it
        String key = currentlyActive ? "taikhoan.locked" : "taikhoan.unlocked";
        redirect.addFlashAttribute("flash", FlashMessage.success(key, email));
        return "redirect:/admin/tai-khoan";
    }

    @PostMapping("/{email}/reset-password")
    public String resetPassword(@PathVariable String email, RedirectAttributes redirect) {
        taiKhoanService.resetPassword(email, "123456");
        redirect.addFlashAttribute("flash", FlashMessage.success("taikhoan.resetPassword.success", email));
        return "redirect:/admin/tai-khoan";
    }
}
