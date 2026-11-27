package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LichGiangDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping({"/giang-vien/lich-giang-day", "/giang-vien/thoi-khoa-bieu"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('GIANG_VIEN')")
public class GiangVienLichDayController {

    private final LichGiangDayService lichGiangDayService;
    private final HocKyService hocKyService;

    @GetMapping
    public String viewTimetable(@RequestParam(required = false) String maHK,
                                @AuthenticationPrincipal AppUserPrincipal principal,
                                Model model) {
        List<HocKyOptionRow> hocKys = hocKyService.findAllOptions();
        if ((maHK == null || maHK.isBlank()) && !hocKys.isEmpty()) {
            HocKyOptionRow current = hocKyService.getCurrentTermOption();
            maHK = current != null ? current.maHK() : hocKys.get(0).maHK();
        }

        List<TimetableSlot> slots = lichGiangDayService.getLecturerTimetable(principal.getMaNS(), maHK);

        model.addAttribute("hocKys", hocKys);
        model.addAttribute("currentHk", maHK);
        model.addAttribute("slots", slots);

        return "giangvien/thoi-khoa-bieu";
    }
}
