package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyDaChonRow;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyItemRow;
import com.qldtkhoa.qldtkhoa_app.dto.HocKyOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDetailView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/sinh-vien/dang-ky-hoc-phan")
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienDangKyController {

    private final DangKyHocPhanService dangKyHocPhanService;
    private final HocKyService hocKyService;
    private final AcademicPolicy academicPolicy;
    private final SinhVienService sinhVienService;

    public SinhVienDangKyController(
            DangKyHocPhanService dangKyHocPhanService,
            HocKyService hocKyService,
            AcademicPolicy academicPolicy,
            SinhVienService sinhVienService) {
        this.dangKyHocPhanService = dangKyHocPhanService;
        this.hocKyService = hocKyService;
        this.academicPolicy = academicPolicy;
        this.sinhVienService = sinhVienService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String maHK,
                        @AuthenticationPrincipal AppUserPrincipal principal,
                        Model model) {
        List<HocKyOptionRow> hocKys = hocKyService.findAllOptions();
        if ((maHK == null || maHK.isBlank()) && !hocKys.isEmpty()) {
            HocKyOptionRow current = hocKyService.getCurrentTermOption();
            maHK = current != null ? current.maHK() : hocKys.get(0).maHK();
        }

        String maSV = principal.getMaSV();
        SinhVienDetailView sv = sinhVienService.getDetailView(maSV);
        List<DangKyItemRow> availableSections = dangKyHocPhanService.getAvailableSections(maSV, maHK);
        List<DangKyDaChonRow> selectedCourses = dangKyHocPhanService.getSelectedCourses(maSV, maHK);

        int totalCredits = selectedCourses.stream()
                .mapToInt(r -> r.soTinChi() != null ? r.soTinChi() : 0)
                .sum();

        model.addAttribute("sinhVien", sv);
        model.addAttribute("hocKys", hocKys);
        model.addAttribute("currentHk", maHK);
        model.addAttribute("availableSections", availableSections);
        model.addAttribute("selectedCourses", selectedCourses);
        model.addAttribute("totalCredits", totalCredits);
        model.addAttribute("policy", academicPolicy);

        return "sinhvien/dang-ky-hoc-phan";
    }

    @PostMapping("/register")
    public String register(@RequestParam String maLHP,
                           @RequestParam String maHK,
                           @AuthenticationPrincipal AppUserPrincipal principal,
                           RedirectAttributes redirect) {
        try {
            dangKyHocPhanService.register(principal.getMaSV(), maLHP);
            redirect.addFlashAttribute("flash", FlashMessage.success("reg.success", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maHK", maHK);
        return "redirect:/sinh-vien/dang-ky-hoc-phan";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String maLHP,
                           @RequestParam String maHK,
                           @AuthenticationPrincipal AppUserPrincipal principal,
                           RedirectAttributes redirect) {
        try {
            dangKyHocPhanService.withdraw(principal.getMaSV(), maLHP);
            redirect.addFlashAttribute("flash", FlashMessage.success("reg.withdrawn", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maHK", maHK);
        return "redirect:/sinh-vien/dang-ky-hoc-phan";
    }
}
