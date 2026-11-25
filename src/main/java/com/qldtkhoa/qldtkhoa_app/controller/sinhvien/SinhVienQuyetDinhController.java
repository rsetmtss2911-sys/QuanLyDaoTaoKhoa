package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping({"/sinh-vien/quyet-dinh", "/sinh-vien/quyet-dinh-hoc-vu"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienQuyetDinhController {

    private final QuyetDinhHocVuService quyetDinhHocVuService;

    @GetMapping
    public String viewDecisions(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<QuyetDinhRow> list = quyetDinhHocVuService.findBySinhVien(principal.getMaSV());
        model.addAttribute("decisions", list);
        return "sinhvien/quyet-dinh";
    }
}
