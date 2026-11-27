package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.GiangVienOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.HocKyOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LichGiangDayService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/xep-lich")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class AdminXepLichController {

    private final LichGiangDayService lichGiangDayService;
    private final LopHocPhanService lopHocPhanService;
    private final HocKyService hocKyService;
    private final NhanSuService nhanSuService;

    private static final List<String> DEFAULT_ROOMS = List.of("301-G2", "302-G2", "201-A2", "205-A2", "PM.101", "PM.102");

    @GetMapping
    public String index(@RequestParam(required = false) String maHK,
                        @RequestParam(defaultValue = "THEO_PHONG") String viewMode,
                        @RequestParam(required = false) String phongHoc,
                        @RequestParam(required = false) String maGV,
                        Model model) {
        List<HocKyOptionRow> hocKys = hocKyService.findAllOptions();
        if ((maHK == null || maHK.isBlank()) && !hocKys.isEmpty()) {
            HocKyOptionRow current = hocKyService.getCurrentTermOption();
            maHK = current != null ? current.maHK() : hocKys.get(0).maHK();
        }

        List<String> distinctRooms = lichGiangDayService.getDistinctRooms(maHK);
        List<String> rooms = new ArrayList<>(distinctRooms);
        for (String r : DEFAULT_ROOMS) {
            if (!rooms.contains(r)) {
                rooms.add(r);
            }
        }

        if ("THEO_PHONG".equalsIgnoreCase(viewMode)) {
            if ((phongHoc == null || phongHoc.isBlank()) && !rooms.isEmpty()) {
                phongHoc = rooms.get(0);
            }
        }

        List<GiangVienOptionRow> giangViens = nhanSuService.findAllGiangVienOptions();
        if ("THEO_GIANG_VIEN".equalsIgnoreCase(viewMode)) {
            if ((maGV == null || maGV.isBlank()) && !giangViens.isEmpty()) {
                maGV = giangViens.get(0).maNS();
            }
        }

        List<TimetableSlot> slots = "THEO_GIANG_VIEN".equalsIgnoreCase(viewMode)
                ? (maGV != null ? lichGiangDayService.getLecturerTimetable(maGV, maHK) : List.of())
                : (phongHoc != null ? lichGiangDayService.getRoomTimetable(phongHoc, maHK) : List.of());

        List<LopHocPhanOptionRow> activeSections = lichGiangDayService.getActiveSectionOptions(maHK);
        List<LopHocPhanOptionRow> unscheduledSections = lichGiangDayService.getUnscheduledSectionOptions(maHK);

        LichGiangDayForm scheduleForm = new LichGiangDayForm();
        if ("THEO_PHONG".equalsIgnoreCase(viewMode) && phongHoc != null && !phongHoc.isBlank()) {
            scheduleForm.setPhongHoc(phongHoc);
        }

        model.addAttribute("hocKys", hocKys);
        model.addAttribute("currentHk", maHK);
        model.addAttribute("viewMode", viewMode);
        model.addAttribute("phongHoc", phongHoc);
        model.addAttribute("maGV", maGV);
        model.addAttribute("rooms", rooms);
        model.addAttribute("giangViens", giangViens);
        model.addAttribute("slots", slots);
        model.addAttribute("activeSections", activeSections);
        model.addAttribute("unscheduledSections", unscheduledSections);
        model.addAttribute("scheduleForm", scheduleForm);

        return "admin/xep-lich/index";
    }

    @PostMapping("/save")
    public String saveSchedule(@RequestParam String maLHP,
                               @Valid @ModelAttribute("scheduleForm") LichGiangDayForm form,
                               BindingResult bindingResult,
                               @RequestParam(required = false) String currentHk,
                               @RequestParam(required = false) String viewMode,
                               @RequestParam(required = false) String currentPhong,
                               @RequestParam(required = false) String currentGv,
                               RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("flash", FlashMessage.danger("lophp.schedule.invalidInput"));
            return buildRedirect(redirect, currentHk, viewMode, currentPhong, currentGv);
        }
        try {
            lopHocPhanService.addSchedule(maLHP, form);
            redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.added"));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return buildRedirect(redirect, currentHk, viewMode, currentPhong, currentGv);
    }

    @PostMapping("/delete")
    public String deleteSchedule(@RequestParam String maLHP,
                                 @RequestParam Integer thuTrongTuan,
                                 @RequestParam Integer tietBatDau,
                                 @RequestParam(required = false) String currentHk,
                                 @RequestParam(required = false) String viewMode,
                                 @RequestParam(required = false) String currentPhong,
                                 @RequestParam(required = false) String currentGv,
                                 RedirectAttributes redirect) {
        try {
            lopHocPhanService.removeSchedule(maLHP, thuTrongTuan, tietBatDau);
            redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.deleted"));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return buildRedirect(redirect, currentHk, viewMode, currentPhong, currentGv);
    }

    private String buildRedirect(RedirectAttributes redirect,
                                 String currentHk,
                                 String viewMode,
                                 String currentPhong,
                                 String currentGv) {
        if (currentHk != null && !currentHk.isBlank()) {
            redirect.addAttribute("maHK", currentHk);
        }
        if (viewMode != null && !viewMode.isBlank()) {
            redirect.addAttribute("viewMode", viewMode);
        }
        if (currentPhong != null && !currentPhong.isBlank()) {
            redirect.addAttribute("phongHoc", currentPhong);
        }
        if (currentGv != null && !currentGv.isBlank()) {
            redirect.addAttribute("maGV", currentGv);
        }
        return "redirect:/admin/xep-lich";
    }
}
