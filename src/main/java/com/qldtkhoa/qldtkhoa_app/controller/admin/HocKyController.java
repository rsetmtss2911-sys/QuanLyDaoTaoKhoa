package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
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
@RequestMapping("/admin/hoc-ky")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class HocKyController {

    private final HocKyService hocKyService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @PageableDefault(size = 20, sort = "namHoc") Pageable pageable,
                       Model model) {
        model.addAttribute("page", hocKyService.search(q, SortWhitelist.of(pageable, "maHK", "tenHK", "namHoc")));
        model.addAttribute("q", q);
        return "admin/hoc-ky/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(Model model) {
        model.addAttribute("form", new HocKyForm());
        model.addAttribute("isEdit", false);
        return "admin/hoc-ky/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") HocKyForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                hocKyService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("hocky.created", form.getTenHK()));
                return "redirect:/admin/hoc-ky";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maHK", ex.getMessageKey(), ex.getArgs(), null);
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey());
            }
        }
        model.addAttribute("isEdit", false);
        return "admin/hoc-ky/form";
    }

    @GetMapping("/{maHK}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maHK, Model model) {
        model.addAttribute("form", hocKyService.getForm(maHK));
        model.addAttribute("isEdit", true);
        return "admin/hoc-ky/form";
    }

    @PostMapping("/{maHK}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maHK,
                         @Valid @ModelAttribute("form") HocKyForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                hocKyService.update(maHK, form);
                redirect.addFlashAttribute("flash", FlashMessage.success("hocky.updated", form.getTenHK()));
                return "redirect:/admin/hoc-ky";
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey());
            }
        }
        form.setMaHK(maHK);
        model.addAttribute("isEdit", true);
        return "admin/hoc-ky/form";
    }

    @PostMapping("/{maHK}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maHK, RedirectAttributes redirect) {
        try {
            hocKyService.delete(maHK);
            redirect.addFlashAttribute("flash", FlashMessage.success("hocky.deleted", maHK));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/hoc-ky";
    }
}
