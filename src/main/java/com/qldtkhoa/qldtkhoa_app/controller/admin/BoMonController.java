package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.BoMonForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.BoMonService;
import com.qldtkhoa.qldtkhoa_app.service.KhoaService;
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
@RequestMapping("/admin/bo-mon")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class BoMonController {

    private final BoMonService boMonService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maKhoa,
                       @PageableDefault(size = 20, sort = "tenBoMon") Pageable pageable,
                       Model model) {
        model.addAttribute("page", boMonService.search(q, maKhoa, SortWhitelist.of(pageable, "maBoMon", "tenBoMon")));
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("maKhoa", maKhoa);
        return "admin/bo-mon/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("form", new BoMonForm());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/bo-mon/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("form") BoMonForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                boMonService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("bomon.created", form.getTenBoMon()));
                return "redirect:/admin/bo-mon";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maBoMon", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/bo-mon/form";
    }

    @GetMapping("/{maBoMon}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable String maBoMon, Model model) {
        model.addAttribute("form", boMonService.getForm(maBoMon));
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/bo-mon/form";
    }

    @PostMapping("/{maBoMon}")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable String maBoMon,
                         @Valid @ModelAttribute("form") BoMonForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaBoMon(maBoMon);
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/bo-mon/form";
        }
        boMonService.update(maBoMon, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("bomon.updated", form.getTenBoMon()));
        return "redirect:/admin/bo-mon";
    }

    @PostMapping("/{maBoMon}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable String maBoMon, RedirectAttributes redirect) {
        try {
            boMonService.delete(maBoMon);
            redirect.addFlashAttribute("flash", FlashMessage.success("bomon.deleted", maBoMon));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/bo-mon";
    }
}
