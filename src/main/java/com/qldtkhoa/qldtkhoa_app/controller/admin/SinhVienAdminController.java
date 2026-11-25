package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienForm;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sinh-vien")
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class SinhVienAdminController {

    private final SinhVienService sinhVienService;
    private final LopHanhChinhService lopHanhChinhService;
    private final QuyetDinhHocVuService quyetDinhHocVuService;
    private final DangKyHocPhanService dangKyHocPhanService;
    private final DiemService diemService;

    public SinhVienAdminController(
            SinhVienService sinhVienService,
            LopHanhChinhService lopHanhChinhService,
            QuyetDinhHocVuService quyetDinhHocVuService,
            DangKyHocPhanService dangKyHocPhanService,
            DiemService diemService) {
        this.sinhVienService = sinhVienService;
        this.lopHanhChinhService = lopHanhChinhService;
        this.quyetDinhHocVuService = quyetDinhHocVuService;
        this.dangKyHocPhanService = dangKyHocPhanService;
        this.diemService = diemService;
    }

    @ModelAttribute("gioiTinhs")
    public GioiTinh[] gioiTinhs() {
        return GioiTinh.values();
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maLopHC,
                       @RequestParam(required = false) TrangThaiSinhVien status,
                       @PageableDefault(size = 20, sort = "hoTen") Pageable pageable,
                       Model model) {
        model.addAttribute("page", sinhVienService.search(q, maLopHC, status, SortWhitelist.of(pageable, "maSV", "hoTen")));
        model.addAttribute("lopHanhChinhs", lopHanhChinhService.findAll());
        model.addAttribute("statuses", TrangThaiSinhVien.values());
        model.addAttribute("q", q);
        model.addAttribute("maLopHC", maLopHC);
        model.addAttribute("status", status);
        return "admin/sinh-vien/list";
    }

    private void populateFormAttributes(Model model) {
        model.addAttribute("gioiTinhs", GioiTinh.values());
        model.addAttribute("lopHanhChinhs", lopHanhChinhService.findAll());
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String newForm(@RequestParam(required = false) String maLopHC, Model model) {
        SinhVienForm form = new SinhVienForm();
        if (maLopHC != null && !maLopHC.isBlank()) {
            form.setMaLopHC(maLopHC);
        }
        model.addAttribute("form", form);
        populateFormAttributes(model);
        model.addAttribute("isEdit", false);
        return "admin/sinh-vien/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String create(@Valid @ModelAttribute("form") SinhVienForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                sinhVienService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("sinhvien.created", form.getHoTen()));
                return "redirect:/admin/sinh-vien";
            } catch (DuplicateResourceException ex) {
                if (ex.getMessageKey().contains("email")) {
                    result.rejectValue("email", ex.getMessageKey(), ex.getArgs(), null);
                } else {
                    result.rejectValue("maSV", ex.getMessageKey(), ex.getArgs(), null);
                }
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            }
        }
        populateFormAttributes(model);
        model.addAttribute("isEdit", false);
        return "admin/sinh-vien/form";
    }

    @GetMapping("/{maSV}")
    public String detail(@PathVariable String maSV, Model model) {
        SinhVien sv = sinhVienService.getWithDetailsOrThrow(maSV);
        model.addAttribute("sv", sv);
        model.addAttribute("quyetDinhs", quyetDinhHocVuService.findBySinhVien(maSV));
        model.addAttribute("dangKys", dangKyHocPhanService.findBySinhVien(maSV));

        BangDiemSinhVienResponse transcript = diemService.getTranscriptView(maSV);
        model.addAttribute("byTerm", transcript.byTerm());
        model.addAttribute("tongTCTichLuy", transcript.tongTCTichLuy());
        model.addAttribute("cpa4", transcript.cpa4());
        model.addAttribute("xepLoai", transcript.xepLoai());
        return "admin/sinh-vien/detail";
    }

    @GetMapping("/{maSV}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String editForm(@PathVariable String maSV, Model model) {
        model.addAttribute("form", sinhVienService.getForm(maSV));
        populateFormAttributes(model);
        model.addAttribute("isEdit", true);
        return "admin/sinh-vien/form";
    }

    @PostMapping("/{maSV}")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String update(@PathVariable String maSV,
                         @Valid @ModelAttribute("form") SinhVienForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            form.setMaSV(maSV);
            populateFormAttributes(model);
            model.addAttribute("isEdit", true);
            return "admin/sinh-vien/form";
        }
        sinhVienService.update(maSV, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("sinhvien.updated", form.getHoTen()));
        return "redirect:/admin/sinh-vien";
    }

    @PostMapping("/{maSV}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
    public String delete(@PathVariable String maSV, RedirectAttributes redirect) {
        try {
            sinhVienService.delete(maSV);
            redirect.addFlashAttribute("flash", FlashMessage.success("sinhvien.deleted", maSV));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/sinh-vien";
    }
}
