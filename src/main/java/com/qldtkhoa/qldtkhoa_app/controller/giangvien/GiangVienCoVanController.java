package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.CoVanClassRosterView;
import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhRow;
import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDangKyRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.ChuongTrinhDaoTaoService;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.HocLucService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/giang-vien/co-van")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienCoVanController {

    private final LopHanhChinhService lopHanhChinhService;
    private final SinhVienService sinhVienService;
    private final HocLucService hocLucService;
    private final ChuongTrinhDaoTaoService ctdtService;
    private final QuyetDinhHocVuService quyetDinhHocVuService;
    private final DangKyHocPhanService dangKyHocPhanService;

    @GetMapping("/lop-hanh-chinh")
    public String myAdvisedClasses(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<LopHanhChinhRow> list = lopHanhChinhService.findByCoVan(principal.getMaNS());
        model.addAttribute("classes", list);
        return "giangvien/co-van/list";
    }

    public String classRoster(String maLopHC, AppUserPrincipal principal, Model model) {
        return classRoster(maLopHC, false, principal, model);
    }

    @GetMapping("/lop-hanh-chinh/{maLopHC}")
    public String classRoster(@PathVariable String maLopHC,
                              @RequestParam(defaultValue = "false") boolean onlyAtRisk,
                              @AuthenticationPrincipal AppUserPrincipal principal,
                              Model model) {
        CoVanClassRosterView roster = lopHanhChinhService.getAdvisedClassRoster(
                maLopHC, principal.getMaNS(), onlyAtRisk);
        model.addAttribute("lop", roster.lop());
        model.addAttribute("students", roster.students());
        model.addAttribute("studentRows", roster.studentRows());
        model.addAttribute("onlyAtRisk", onlyAtRisk);
        model.addAttribute("atRiskCount", roster.atRiskCount());
        model.addAttribute("totalStudents", roster.totalStudents());
        return "giangvien/co-van/detail";
    }

    @GetMapping("/sinh-vien/{maSV}")
    public String studentDetail(@PathVariable String maSV,
                                @AuthenticationPrincipal AppUserPrincipal principal,
                                Model model) {
        SinhVienDetailView sv = sinhVienService.getAdvisedStudentDetail(maSV, principal.getMaNS());
        BangDiemSinhVienResponse transcript = hocLucService.evaluateStudent(maSV);
        TienDoHocTapView tienDo = ctdtService.getTienDoCTDT(maSV);
        List<QuyetDinhRow> decisions = quyetDinhHocVuService.findBySinhVien(maSV);
        List<SinhVienDangKyRow> registrations = dangKyHocPhanService.findBySinhVien(maSV);

        model.addAttribute("sv", sv);
        model.addAttribute("transcript", transcript);
        model.addAttribute("tienDo", tienDo);
        model.addAttribute("decisions", decisions);
        model.addAttribute("registrations", registrations);
        return "giangvien/co-van/sinh-vien-detail";
    }
}
