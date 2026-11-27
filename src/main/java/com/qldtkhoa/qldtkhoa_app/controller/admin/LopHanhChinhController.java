package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import com.qldtkhoa.qldtkhoa_app.service.KhoaNhapHocService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.NganhHocService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
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
@RequestMapping("/admin/lop-hanh-chinh")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class LopHanhChinhController {

    private final LopHanhChinhService lopHanhChinhService;
    private final KhoaNhapHocService khoaNhapHocService;
    private final NganhHocService nganhHocService;
    private final NhanSuService nhanSuService;
    private final SinhVienService sinhVienService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String maNganh,
            @RequestParam(required = false) String maKhoaHoc,
            @PageableDefault(size = 20, sort = "tenLopHC") Pageable pageable,
            Model model) {
        model.addAttribute("page",
                lopHanhChinhService.search(q, maNganh, maKhoaHoc, SortWhitelist.of(pageable, "maLopHC", "tenLopHC")));
        model.addAttribute("nganhs", nganhHocService.findAll());
        model.addAttribute("khoaHocs", khoaNhapHocService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("maNganh", maNganh);
        model.addAttribute("maKhoaHoc", maKhoaHoc);
        return "admin/lop-hanh-chinh/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(Model model) {
        model.addAttribute("form", new LopHanhChinhForm());
        populateSelects(model);
        model.addAttribute("isEdit", false);
        return "admin/lop-hanh-chinh/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") LopHanhChinhForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                lopHanhChinhService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("lophc.created", form.getTenLopHC()));
                return "redirect:/admin/lop-hanh-chinh";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maLopHC", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        populateSelects(model);
        model.addAttribute("isEdit", false);
        return "admin/lop-hanh-chinh/form";
    }

    @GetMapping("/{maLopHC}")
    public String detail(@PathVariable String maLopHC, Model model) {
        LopHanhChinhDetailView lhc = lopHanhChinhService.getDetailView(maLopHC);
        model.addAttribute("lop", lhc);
        model.addAttribute("sinhViens", sinhVienService.findByLopHanhChinh(maLopHC));
        return "admin/lop-hanh-chinh/detail";
    }

    @GetMapping("/{maLopHC}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maLopHC, Model model) {
        model.addAttribute("form", lopHanhChinhService.getForm(maLopHC));
        populateSelects(model);
        model.addAttribute("isEdit", true);
        return "admin/lop-hanh-chinh/form";
    }

    @PostMapping("/{maLopHC}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maLopHC,
            @Valid @ModelAttribute("form") LopHanhChinhForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaLopHC(maLopHC);
            populateSelects(model);
            model.addAttribute("isEdit", true);
            return "admin/lop-hanh-chinh/form";
        }
        lopHanhChinhService.update(maLopHC, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("lophc.updated", form.getTenLopHC()));
        return "redirect:/admin/lop-hanh-chinh";
    }

    @PostMapping("/{maLopHC}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maLopHC, RedirectAttributes redirect) {
        try {
            lopHanhChinhService.delete(maLopHC);
            redirect.addFlashAttribute("flash", FlashMessage.success("lophc.deleted", maLopHC));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/lop-hanh-chinh";
    }

    private void populateSelects(Model model) {
        model.addAttribute("nganhs", nganhHocService.findAll());
        model.addAttribute("khoaHocs", khoaNhapHocService.findAll());
        model.addAttribute("giangViens", nhanSuService.findAllGiangVien());
    }
}
