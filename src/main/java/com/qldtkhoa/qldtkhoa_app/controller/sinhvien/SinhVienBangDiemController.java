package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sinh-vien/bang-diem")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienBangDiemController {

    private final DiemService diemService;

    @GetMapping
    public String viewTranscript(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        BangDiemSinhVienResponse res = diemService.getTranscriptView(principal.getMaSV());
        model.addAttribute("byTerm", res.byTerm());
        model.addAttribute("tongTCTichLuy", res.tongTCTichLuy());
        model.addAttribute("cpa4", res.cpa4());
        model.addAttribute("xepLoai", res.xepLoai());
        model.addAttribute("termSummaries", res.termSummaries());
        model.addAttribute("canhBaoHocVu", res.canhBaoHocVu());
        model.addAttribute("lyDoCanhBao", res.lyDoCanhBao());
        return "sinhvien/bang-diem";
    }
}

