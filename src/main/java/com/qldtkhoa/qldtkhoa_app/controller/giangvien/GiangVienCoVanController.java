package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/giang-vien/co-van/lop-hanh-chinh")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienCoVanController {

    private final LopHanhChinhService lopHanhChinhService;
    private final SinhVienService sinhVienService;

    @GetMapping
    public String myAdvisedClasses(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<LopHanhChinhRow> list = lopHanhChinhService.findByCoVan(principal.getMaNS());
        model.addAttribute("classes", list);
        return "giangvien/co-van/list";
    }

    @GetMapping("/{maLopHC}")
    public String classRoster(@PathVariable String maLopHC,
                              @AuthenticationPrincipal AppUserPrincipal principal,
                              Model model) {
        LopHanhChinh lhc = lopHanhChinhService.getWithDetailsOrThrow(maLopHC);
        if (lhc.getCoVan() == null || !lhc.getCoVan().getMaNS().equalsIgnoreCase(principal.getMaNS())) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không phải là Cố vấn học tập của lớp hành chính này!");
        }
        List<SinhVienRow> students = sinhVienService.findByLopHanhChinh(maLopHC);

        model.addAttribute("lop", lhc);
        model.addAttribute("students", students);
        return "giangvien/co-van/detail";
    }
}
