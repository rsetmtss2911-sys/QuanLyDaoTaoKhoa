package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LichGiangDayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/sinh-vien/thoi-khoa-bieu")
@PreAuthorize("hasRole('SINH_VIEN')")
public class SinhVienThoiKhoaBieuController {

    private final LichGiangDayService lichGiangDayService;
    private final HocKyService hocKyService;

    public SinhVienThoiKhoaBieuController(LichGiangDayService lichGiangDayService, HocKyService hocKyService) {
        this.lichGiangDayService = lichGiangDayService;
        this.hocKyService = hocKyService;
    }

    @GetMapping
    public String viewTimetable(@RequestParam(required = false) String maHK,
                                @AuthenticationPrincipal AppUserPrincipal principal,
                                Model model) {
        List<HocKyOptionRow> hocKys = hocKyService.findAllOptions();
        if ((maHK == null || maHK.isBlank()) && !hocKys.isEmpty()) {
            HocKyOptionRow current = hocKyService.getCurrentTermOption();
            maHK = current != null ? current.maHK() : hocKys.get(0).maHK();
        }

        List<TimetableSlot> slots = lichGiangDayService.getStudentTimetable(principal.getMaSV(), maHK);
        var unscheduled = lichGiangDayService.getUnscheduledRegisteredCourses(principal.getMaSV(), maHK);
        var registeredCourses = lichGiangDayService.getRegisteredCoursesSummary(principal.getMaSV(), maHK);
        int totalCredits = registeredCourses.stream()
                .mapToInt(c -> c.soTinChi() != null ? c.soTinChi() : 0)
                .sum();

        model.addAttribute("hocKys", hocKys);
        model.addAttribute("currentHk", maHK);
        model.addAttribute("slots", slots);
        model.addAttribute("unscheduledCourses", unscheduled);
        model.addAttribute("registeredCourses", registeredCourses);
        model.addAttribute("totalRegisteredCredits", totalCredits);

        return "sinhvien/thoi-khoa-bieu";
    }
}
