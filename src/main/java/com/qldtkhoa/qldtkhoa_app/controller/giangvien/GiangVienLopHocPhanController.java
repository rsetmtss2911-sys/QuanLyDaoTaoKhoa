package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/giang-vien/lop-hoc-phan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienLopHocPhanController {

    private final LopHocPhanService lopHocPhanService;
    private final DiemService diemService;
    private final HocKyService hocKyService;

    @GetMapping
    public String list(@RequestParam(required = false) String maHK,
                       @AuthenticationPrincipal AppUserPrincipal principal,
                       Model model) {
        var hocKys = hocKyService.findAll();
        if ((maHK == null || maHK.isBlank()) && !hocKys.isEmpty()) {
            HocKyNamHoc current = hocKyService.getCurrentTerm();
            maHK = current != null ? current.getMaHK() : hocKys.get(0).getMaHK();
        }

        var sectionRows = lopHocPhanService.findByLecturerAndTerm(principal.getMaNS(), maHK);

        model.addAttribute("hocKys", hocKys);
        model.addAttribute("currentHk", maHK);
        model.addAttribute("sections", sectionRows);

        return "giangvien/lop-hoc-phan/list";
    }

    @GetMapping("/{maLHP}/diem")
    public String gradeSheet(@PathVariable String maLHP,
                             @AuthenticationPrincipal AppUserPrincipal principal,
                             Model model) {
        LopHocPhan lhp = lopHocPhanService.getWithDetailsOrThrow(maLHP);
        if (!lhp.getGiangVien().getMaNS().equalsIgnoreCase(principal.getMaNS())) {
            throw new AccessDeniedException("Bạn không phải là giảng viên phụ trách lớp học phần này!");
        }

        List<DiemRow> rows = diemService.getGradeSheet(maLHP);
        model.addAttribute("lop", lhp);
        model.addAttribute("rows", rows);
        model.addAttribute("form", new BangDiemLopForm());

        return "giangvien/lop-hoc-phan/diem";
    }

    @PostMapping("/{maLHP}/diem")
    public String saveGrades(@PathVariable String maLHP,
                             @Valid @ModelAttribute("form") BangDiemLopForm form,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal AppUserPrincipal principal,
                             RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("flash", FlashMessage.danger("grade.scoreRange"));
            return "redirect:/giang-vien/lop-hoc-phan/" + maLHP + "/diem";
        }
        try {
            diemService.saveGradeSheet(maLHP, form, principal.getMaNS(), false);
            redirect.addFlashAttribute("flash", FlashMessage.success("grade.saved", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/giang-vien/lop-hoc-phan/" + maLHP + "/diem";
    }
}
