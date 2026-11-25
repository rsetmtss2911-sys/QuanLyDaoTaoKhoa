package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.BoMonService;
import com.qldtkhoa.qldtkhoa_app.service.KhoaService;
import com.qldtkhoa.qldtkhoa_app.service.NganhHocService;
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
@RequestMapping("/admin/khoa")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class KhoaController {

    private final KhoaService khoaService;
    private final BoMonService boMonService;
    private final NganhHocService nganhHocService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @PageableDefault(size = 20, sort = "tenKhoa") Pageable pageable,
                       Model model) {
        model.addAttribute("page", khoaService.search(q, SortWhitelist.of(pageable, "maKhoa", "tenKhoa", "namThanhLap")));
        model.addAttribute("q", q);
        return "admin/khoa/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("form", new KhoaForm());
        model.addAttribute("isEdit", false);
        return "admin/khoa/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("form") KhoaForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                khoaService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("khoa.created", form.getTenKhoa()));
                return "redirect:/admin/khoa";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maKhoa", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        model.addAttribute("isEdit", false);
        return "admin/khoa/form";
    }

    @GetMapping("/{maKhoa}")
    public String detail(@PathVariable String maKhoa, Model model) {
        var khoa = khoaService.getOrThrow(maKhoa);
        model.addAttribute("khoa", khoa);
        model.addAttribute("boMons", boMonService.findByKhoa(maKhoa));
        model.addAttribute("nganhHocs", nganhHocService.findByKhoa(maKhoa));
        return "admin/khoa/detail";
    }

    @GetMapping("/{maKhoa}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable String maKhoa, Model model) {
        model.addAttribute("form", khoaService.getForm(maKhoa));
        model.addAttribute("isEdit", true);
        return "admin/khoa/form";
    }

    @PostMapping("/{maKhoa}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable String maKhoa,
                         @Valid @ModelAttribute("form") KhoaForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaKhoa(maKhoa);
            model.addAttribute("isEdit", true);
            return "admin/khoa/form";
        }
        khoaService.update(maKhoa, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("khoa.updated", form.getTenKhoa()));
        return "redirect:/admin/khoa";
    }

    @PostMapping("/{maKhoa}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String maKhoa, RedirectAttributes redirect) {
        try {
            khoaService.delete(maKhoa);
            redirect.addFlashAttribute("flash", FlashMessage.success("khoa.deleted", maKhoa));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/khoa";
    }
}
