package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.NganhHocForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
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
@RequestMapping("/admin/nganh-hoc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class NganhHocController {

    private final NganhHocService nganhHocService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maKhoa,
                       @PageableDefault(size = 20, sort = "tenNganh") Pageable pageable,
                       Model model) {
        model.addAttribute("page", nganhHocService.search(q, maKhoa, SortWhitelist.of(pageable, "maNganh", "tenNganh")));
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("maKhoa", maKhoa);
        return "admin/nganh-hoc/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(Model model) {
        model.addAttribute("form", new NganhHocForm());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/nganh-hoc/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") NganhHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                nganhHocService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("nganh.created", form.getTenNganh()));
                return "redirect:/admin/nganh-hoc";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maNganh", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/nganh-hoc/form";
    }

    @GetMapping("/{maNganh}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maNganh, Model model) {
        model.addAttribute("form", nganhHocService.getForm(maNganh));
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/nganh-hoc/form";
    }

    @PostMapping("/{maNganh}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maNganh,
                         @Valid @ModelAttribute("form") NganhHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaNganh(maNganh);
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/nganh-hoc/form";
        }
        nganhHocService.update(maNganh, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("nganh.updated", form.getTenNganh()));
        return "redirect:/admin/nganh-hoc";
    }

    @PostMapping("/{maNganh}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maNganh, RedirectAttributes redirect) {
        try {
            nganhHocService.delete(maNganh);
            redirect.addFlashAttribute("flash", FlashMessage.success("nganh.deleted", maNganh));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/nganh-hoc";
    }
}
