package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.ChuongTrinhDaoTaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sinh-vien/tien-do-ctdt")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienTienDoController {

    private final ChuongTrinhDaoTaoService ctdtService;

    @GetMapping
    public String viewProgress(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        TienDoHocTapView tienDo = ctdtService.getTienDoCTDT(principal.getMaSV());

        model.addAttribute("tienDo", tienDo);
        model.addAttribute("tenNganh", tienDo.tenNganh());
        model.addAttribute("maKhoaHoc", tienDo.maKhoaHoc());
        model.addAttribute("curriculum", tienDo.curriculum());
        model.addAttribute("passedSubjects", tienDo.passedSubjects());
        model.addAttribute("totalCurriculumCredits", tienDo.totalCurriculumCredits());
        model.addAttribute("completedCredits", tienDo.completedCredits());
        model.addAttribute("progressPercent", tienDo.progressPercent());
        model.addAttribute("blockProgress", tienDo.blockProgress());
        model.addAttribute("courseStatuses", tienDo.courseStatuses());

        return "sinhvien/tien-do-ctdt";
    }
}
