package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.*;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/giang-vien/co-van")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienCoVanController {

    private final LopHanhChinhService lopHanhChinhService;
    private final SinhVienService sinhVienService;
    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
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
        LopHanhChinh lhc = lopHanhChinhService.getWithDetailsOrThrow(maLopHC);
        if (lhc.getCoVan() == null || !lhc.getCoVan().getMaNS().equalsIgnoreCase(principal.getMaNS())) {
            throw new AccessDeniedException("Bạn không phải là Cố vấn học tập của lớp hành chính này!");
        }
        List<SinhVienRow> students = sinhVienService.findByLopHanhChinh(maLopHC);

        List<String> maSVs = students.stream().map(sv -> sv.maSV()).toList();
        Map<String, List<BangDiemSinhVienView>> gradesBySv = maSVs.isEmpty()
                ? Map.of()
                : bangDiemViewRepository.findById_MaSVInOrderByNamHocDescMaHKAscTenMonAsc(maSVs).stream()
                        .collect(Collectors.groupingBy(v -> v.getMaSV()));

        List<CoVanStudentRow> allStudentRows = students.stream()
                .map(sv -> {
                    HocLucSummary summary = hocLucService.evaluateSummary(gradesBySv.getOrDefault(sv.maSV(), List.of()));
                    return new CoVanStudentRow(
                            sv.maSV(), sv.hoTen(), sv.gioiTinh(), sv.ngaySinh(), sv.email(),
                            sv.trangThaiHienTai(),
                            summary.cpa4(), summary.xepLoai(), summary.tongTCTichLuy(),
                            summary.soMonTruot(), summary.coCamThi(),
                            summary.canhBaoHocVu(), summary.lyDoCanhBao()
                    );
                })
                .toList();

        long atRiskCount = allStudentRows.stream().filter(r -> r.isAtRisk()).count();
        List<CoVanStudentRow> displayedRows = onlyAtRisk
                ? allStudentRows.stream().filter(r -> r.isAtRisk()).toList()
                : allStudentRows;

        model.addAttribute("lop", lhc);
        model.addAttribute("students", students);
        model.addAttribute("studentRows", displayedRows);
        model.addAttribute("onlyAtRisk", onlyAtRisk);
        model.addAttribute("atRiskCount", atRiskCount);
        model.addAttribute("totalStudents", allStudentRows.size());
        return "giangvien/co-van/detail";
    }

    @GetMapping("/sinh-vien/{maSV}")
    public String studentDetail(@PathVariable String maSV,
                                @AuthenticationPrincipal AppUserPrincipal principal,
                                Model model) {
        SinhVien sv = sinhVienService.getWithDetailsOrThrow(maSV);
        if (sv.getLopHanhChinh() == null
                || sv.getLopHanhChinh().getCoVan() == null
                || !sv.getLopHanhChinh().getCoVan().getMaNS().equalsIgnoreCase(principal.getMaNS())) {
            throw new AccessDeniedException("Bạn không phải là Cố vấn học tập của sinh viên này!");
        }

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
