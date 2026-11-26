package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.MonHocForm;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.BoMonService;
import com.qldtkhoa.qldtkhoa_app.service.MonHocService;
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
@RequestMapping("/admin/mon-hoc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class MonHocController {

    private final MonHocService monHocService;
    private final BoMonService boMonService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maBoMon,
                       @PageableDefault(size = 20, sort = "tenMon") Pageable pageable,
                       Model model) {
        model.addAttribute("page", monHocService.search(q, maBoMon, SortWhitelist.of(pageable, "maMon", "tenMon", "soTinChi")));
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("maBoMon", maBoMon);
        return "admin/mon-hoc/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(Model model) {
        model.addAttribute("form", new MonHocForm());
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/mon-hoc/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") MonHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                monHocService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("monhoc.created", form.getTenMon()));
                return "redirect:/admin/mon-hoc";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maMon", ex.getMessageKey(), ex.getArgs(), null);
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            }
        }
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", false);
        return "admin/mon-hoc/form";
    }

    @GetMapping("/{maMon}")
    public String detail(@PathVariable String maMon, Model model) {
        MonHoc mh = monHocService.getWithDetailsOrThrow(maMon);
        model.addAttribute("mon", mh);
        model.addAttribute("prereqs", monHocService.findPrerequisites(maMon));
        model.addAttribute("allMonHocs", monHocService.findAll());
        return "admin/mon-hoc/detail";
    }

    @GetMapping("/{maMon}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maMon, Model model) {
        model.addAttribute("form", monHocService.getForm(maMon));
        model.addAttribute("boMons", boMonService.findAll());
        model.addAttribute("isEdit", true);
        return "admin/mon-hoc/form";
    }

    @PostMapping("/{maMon}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maMon,
                         @Valid @ModelAttribute("form") MonHocForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaMon(maMon);
            model.addAttribute("boMons", boMonService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/mon-hoc/form";
        }
        try {
            monHocService.update(maMon, form);
            redirect.addFlashAttribute("flash", FlashMessage.success("monhoc.updated", form.getTenMon()));
            return "redirect:/admin/mon-hoc";
        } catch (BusinessRuleException ex) {
            result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            form.setMaMon(maMon);
            model.addAttribute("boMons", boMonService.findAll());
            model.addAttribute("isEdit", true);
            return "admin/mon-hoc/form";
        }
    }

    @PostMapping("/{maMon}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maMon, RedirectAttributes redirect) {
        try {
            monHocService.delete(maMon);
            redirect.addFlashAttribute("flash", FlashMessage.success("monhoc.deleted", maMon));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/mon-hoc";
    }

    @PostMapping("/{maMon}/prerequisites")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String addPrerequisite(@PathVariable String maMon,
                                  @RequestParam String maMonTienQuyet,
                                  RedirectAttributes redirect) {
        try {
            monHocService.addPrerequisite(maMon, maMonTienQuyet);
            redirect.addFlashAttribute("flash", FlashMessage.success("monhoc.tienQuyet.added", maMonTienQuyet, maMon));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maMon", maMon);
        return "redirect:/admin/mon-hoc/{maMon}";
    }

    @PostMapping("/{maMon}/prerequisites/{maMonTienQuyet}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String removePrerequisite(@PathVariable String maMon,
                                     @PathVariable String maMonTienQuyet,
                                     RedirectAttributes redirect) {
        monHocService.removePrerequisite(maMon, maMonTienQuyet);
        redirect.addFlashAttribute("flash", FlashMessage.success("monhoc.tienQuyet.removed", maMonTienQuyet, maMon));
        redirect.addAttribute("maMon", maMon);
        return "redirect:/admin/mon-hoc/{maMon}";
    }
}
