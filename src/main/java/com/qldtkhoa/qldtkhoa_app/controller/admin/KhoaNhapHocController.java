package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaNhapHocForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.KhoaNhapHocService;
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
@RequestMapping("/admin/khoa-nhap-hoc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class KhoaNhapHocController {

    private final KhoaNhapHocService khoaNhapHocService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @PageableDefault(size = 20, sort = "namBatDau") Pageable pageable,
                       Model model) {
        model.addAttribute("page", khoaNhapHocService.search(q, SortWhitelist.of(pageable, "maKhoaHoc", "namBatDau")));
        model.addAttribute("q", q);
        return "admin/khoa-nhap-hoc/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(Model model) {
        model.addAttribute("form", new KhoaNhapHocForm());
        model.addAttribute("isEdit", false);
        return "admin/khoa-nhap-hoc/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") KhoaNhapHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                khoaNhapHocService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("khoahoc.created", form.getMaKhoaHoc()));
                return "redirect:/admin/khoa-nhap-hoc";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maKhoaHoc", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        model.addAttribute("isEdit", false);
        return "admin/khoa-nhap-hoc/form";
    }

    @GetMapping("/{maKhoaHoc}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maKhoaHoc, Model model) {
        model.addAttribute("form", khoaNhapHocService.getForm(maKhoaHoc));
        model.addAttribute("isEdit", true);
        return "admin/khoa-nhap-hoc/form";
    }

    @PostMapping("/{maKhoaHoc}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maKhoaHoc,
                         @Valid @ModelAttribute("form") KhoaNhapHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaKhoaHoc(maKhoaHoc);
            model.addAttribute("isEdit", true);
            return "admin/khoa-nhap-hoc/form";
        }
        khoaNhapHocService.update(maKhoaHoc, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("khoahoc.updated", form.getMaKhoaHoc()));
        return "redirect:/admin/khoa-nhap-hoc";
    }

    @PostMapping("/{maKhoaHoc}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maKhoaHoc, RedirectAttributes redirect) {
        try {
            khoaNhapHocService.delete(maKhoaHoc);
            redirect.addFlashAttribute("flash", FlashMessage.success("khoahoc.deleted", maKhoaHoc));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/khoa-nhap-hoc";
    }
}
