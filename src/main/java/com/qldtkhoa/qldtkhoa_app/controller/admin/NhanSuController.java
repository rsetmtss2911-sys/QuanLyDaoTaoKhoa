package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.NhanSuForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.BoMonService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.service.VaiTroService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nhan-su")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class NhanSuController {

    private final NhanSuService nhanSuService;
    private final BoMonService boMonService;
    private final VaiTroService vaiTroService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String role,
                       @PageableDefault(size = 20, sort = "hoTen") Pageable pageable,
                       Model model) {
        model.addAttribute("page", nhanSuService.search(q, role, SortWhitelist.of(pageable, "maNS", "hoTen")));
        model.addAttribute("vaiTros", vaiTroService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("role", role);
        return "admin/nhan-su/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("form", new NhanSuForm());
        model.addAttribute("vaiTros", vaiTroService.findAll());
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/nhan-su/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("form") NhanSuForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                nhanSuService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("nhansu.created", form.getHoTen()));
                return "redirect:/admin/nhan-su";
            } catch (DuplicateResourceException ex) {
                if (ex.getMessageKey().contains("email")) {
                    result.rejectValue("email", ex.getMessageKey(), ex.getArgs(), null);
                } else {
                    result.rejectValue("maNS", ex.getMessageKey(), ex.getArgs(), null);
                }
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            }
        }
        model.addAttribute("vaiTros", vaiTroService.findAll());
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/nhan-su/form";
    }

    @GetMapping("/{maNS}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable String maNS, Model model) {
        model.addAttribute("form", nhanSuService.getForm(maNS));
        model.addAttribute("vaiTros", vaiTroService.findAll());
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/nhan-su/form";
    }

    @PostMapping("/{maNS}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable String maNS,
                         @Valid @ModelAttribute("form") NhanSuForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaNS(maNS);
            model.addAttribute("vaiTros", vaiTroService.findAll());
            model.addAttribute("boMons", boMonService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/nhan-su/form";
        }
        try {
            nhanSuService.update(maNS, form);
            redirect.addFlashAttribute("flash", FlashMessage.success("nhansu.updated", form.getHoTen()));
            return "redirect:/admin/nhan-su";
        } catch (BusinessRuleException ex) {
            result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            form.setMaNS(maNS);
            model.addAttribute("vaiTros", vaiTroService.findAll());
            model.addAttribute("boMons", boMonService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/nhan-su/form";
        }
    }

    @PostMapping("/{maNS}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String maNS, RedirectAttributes redirect) {
        try {
            nhanSuService.delete(maNS);
            redirect.addFlashAttribute("flash", FlashMessage.success("nhansu.deleted", maNS));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/nhan-su";
    }
}
