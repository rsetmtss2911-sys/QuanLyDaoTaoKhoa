package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanForm;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.MonHocService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/lop-hoc-phan")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class LopHocPhanAdminController {

    private final LopHocPhanService lopHocPhanService;
    private final MonHocService monHocService;
    private final HocKyService hocKyService;
    private final NhanSuService nhanSuService;
    private final DiemService diemService;

    @InitBinder("form")
    public void initFormBinder(WebDataBinder binder) {
        binder.setAllowedFields("maLHP", "maMon", "maGV", "maHK", "siSoToiDa", "trangThaiLop");
    }

    @InitBinder("gradeForm")
    public void initGradeFormBinder(WebDataBinder binder) {
        binder.setAllowedFields("entries*", "lyDoChinhSua");
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maHK,
                       @RequestParam(required = false) String maMon,
                       @RequestParam(required = false) String maGV,
                       @RequestParam(required = false) TrangThaiLop status,
                       @PageableDefault(size = 20, sort = "maLHP") Pageable pageable,
                       Model model) {
        model.addAttribute("page", lopHocPhanService.search(q, maHK, maMon, maGV, status, SortWhitelist.of(pageable, "maLHP", "siSoToiDa")));
        populateFilters(model);
        model.addAttribute("q", q);
        model.addAttribute("maHK", maHK);
        model.addAttribute("maMon", maMon);
        model.addAttribute("maGV", maGV);
        model.addAttribute("status", status);
        return "admin/lop-hoc-phan/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new LopHocPhanForm());
        populateSelects(model);
        model.addAttribute("isEdit", false);
        return "admin/lop-hoc-phan/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") LopHocPhanForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                lopHocPhanService.create(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("lophocphan.created", form.getMaLHP()));
                return "redirect:/admin/lop-hoc-phan";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maLHP", ex.getMessageKey(), ex.getArgs(), null);
            }
        }
        populateSelects(model);
        model.addAttribute("isEdit", false);
        return "admin/lop-hoc-phan/form";
    }

    @GetMapping("/{maLHP}")
    public String detail(@PathVariable String maLHP, Model model) {
        LopHocPhanDetailView lhp = lopHocPhanService.getDetailView(maLHP);
        model.addAttribute("lop", lhp);
        model.addAttribute("schedules", lopHocPhanService.findScheduleViews(maLHP));
        model.addAttribute("registrations", lopHocPhanService.findRegistrationRows(maLHP));
        model.addAttribute("activeCount", lopHocPhanService.countActiveRegistrations(maLHP));
        model.addAttribute("scheduleForm", new LichGiangDayForm());
        return "admin/lop-hoc-phan/detail";
    }

    @GetMapping("/{maLHP}/edit")
    public String editForm(@PathVariable String maLHP, Model model) {
        model.addAttribute("form", lopHocPhanService.getForm(maLHP));
        populateSelects(model);
        model.addAttribute("isEdit", true);
        return "admin/lop-hoc-phan/form";
    }

    @PostMapping("/{maLHP}")
    public String update(@PathVariable String maLHP,
                         @Valid @ModelAttribute("form") LopHocPhanForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                lopHocPhanService.update(maLHP, form);
                redirect.addFlashAttribute("flash", FlashMessage.success("lophocphan.updated", maLHP));
                return "redirect:/admin/lop-hoc-phan";
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            }
        }
        form.setMaLHP(maLHP);
        populateSelects(model);
        model.addAttribute("isEdit", true);
        return "admin/lop-hoc-phan/form";
    }

    @PostMapping("/{maLHP}/lock-enrollment")
    public String lockEnrollment(@PathVariable String maLHP,
                                 @AuthenticationPrincipal AppUserPrincipal principal,
                                 RedirectAttributes redirect) {
        try {
            lopHocPhanService.lockEnrollment(maLHP, principal != null ? principal.getEmail() : "SYSTEM");
            redirect.addFlashAttribute("flash", FlashMessage.success("lophp.lock.success", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maLHP", maLHP);
        return "redirect:/admin/lop-hoc-phan/{maLHP}";
    }

    @PostMapping("/{maLHP}/lock-grades")
    public String lockGrades(@PathVariable String maLHP,
                             @AuthenticationPrincipal AppUserPrincipal principal,
                             RedirectAttributes redirect) {
        try {
            lopHocPhanService.lockGrades(maLHP, principal != null ? principal.getEmail() : "SYSTEM");
            redirect.addFlashAttribute("flash", FlashMessage.success("lophp.lockGrades.success", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maLHP", maLHP);
        return "redirect:/admin/lop-hoc-phan/{maLHP}";
    }

    @PostMapping("/{maLHP}/cancel")
    public String cancelSection(@PathVariable String maLHP,
                                @RequestParam(defaultValue = "") String lyDo,
                                @AuthenticationPrincipal AppUserPrincipal principal,
                                RedirectAttributes redirect) {
        try {
            int withdrawn = lopHocPhanService.cancelSection(
                    maLHP, lyDo, principal != null ? principal.getEmail() : "SYSTEM");
            redirect.addFlashAttribute("flash", FlashMessage.success("lophp.cancel.success", maLHP, withdrawn));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maLHP", maLHP);
        return "redirect:/admin/lop-hoc-phan/{maLHP}";
    }

    @GetMapping("/{maLHP}/diem")
    public String gradeSheet(@PathVariable String maLHP, Model model) {
        LopHocPhanDetailView lhp = lopHocPhanService.getDetailView(maLHP);
        model.addAttribute("lop", lhp);
        model.addAttribute("rows", diemService.getGradeSheet(maLHP));
        model.addAttribute("form", new BangDiemLopForm());
        return "admin/lop-hoc-phan/diem";
    }

    @GetMapping("/{maLHP}/diem/export-csv")
    public ResponseEntity<byte[]> exportGradeSheetCsv(@PathVariable String maLHP) {
        lopHocPhanService.getDetailView(maLHP);
        return diemService.exportGradeSheetCsv(maLHP);
    }

    @PostMapping("/{maLHP}/diem")
    @PreAuthorize("hasRole('PHONG_DAO_TAO')")
    public String saveGrades(@PathVariable String maLHP,
                             @Valid @ModelAttribute("gradeForm") BangDiemLopForm form,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal AppUserPrincipal principal,
                             Model model,
                             RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            populateGradeSheetModelForError(maLHP, form, model, FlashMessage.danger("grade.scoreRange"));
            return "admin/lop-hoc-phan/diem";
        }
        try {
            String actor = principal != null ? principal.getEmail() : "PHONG_DAO_TAO";
            diemService.saveGradeSheet(maLHP, form, actor, true);
            redirect.addFlashAttribute("flash", FlashMessage.success("grade.saved", maLHP));
            redirect.addAttribute("maLHP", maLHP);
            return "redirect:/admin/lop-hoc-phan/{maLHP}/diem";
        } catch (BusinessRuleException ex) {
            populateGradeSheetModelForError(maLHP, form, model, FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
            return "admin/lop-hoc-phan/diem";
        }
    }

    @PostMapping("/{maLHP}/delete")
    public String delete(@PathVariable String maLHP, RedirectAttributes redirect) {
        try {
            lopHocPhanService.delete(maLHP);
            redirect.addFlashAttribute("flash", FlashMessage.success("lophocphan.deleted", maLHP));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        return "redirect:/admin/lop-hoc-phan";
    }

    @PostMapping("/{maLHP}/schedules")
    public String addSchedule(@PathVariable String maLHP,
                              @Valid @ModelAttribute("scheduleForm") LichGiangDayForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("lop", lopHocPhanService.getDetailView(maLHP));
            model.addAttribute("schedules", lopHocPhanService.findScheduleViews(maLHP));
            model.addAttribute("registrations", lopHocPhanService.findRegistrationRows(maLHP));
            model.addAttribute("activeCount", lopHocPhanService.countActiveRegistrations(maLHP));
            return "admin/lop-hoc-phan/detail";
        }
        try {
            lopHocPhanService.addSchedule(maLHP, form);
            redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.added"));
            redirect.addAttribute("maLHP", maLHP);
            return "redirect:/admin/lop-hoc-phan/{maLHP}";
        } catch (BusinessRuleException ex) {
            bindingResult.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            model.addAttribute("lop", lopHocPhanService.getDetailView(maLHP));
            model.addAttribute("schedules", lopHocPhanService.findScheduleViews(maLHP));
            model.addAttribute("registrations", lopHocPhanService.findRegistrationRows(maLHP));
            model.addAttribute("activeCount", lopHocPhanService.countActiveRegistrations(maLHP));
            return "admin/lop-hoc-phan/detail";
        }
    }

    @PostMapping("/{maLHP}/schedules/delete")
    public String removeSchedule(@PathVariable String maLHP,
                                 @RequestParam Integer thuTrongTuan,
                                 @RequestParam Integer tietBatDau,
                                 RedirectAttributes redirect) {
        try {
            lopHocPhanService.removeSchedule(maLHP, thuTrongTuan, tietBatDau);
            redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.deleted"));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maLHP", maLHP);
        return "redirect:/admin/lop-hoc-phan/{maLHP}";
    }

    private void populateGradeSheetModelForError(String maLHP, BangDiemLopForm form, Model model, FlashMessage flash) {
        LopHocPhanDetailView lhp = lopHocPhanService.getDetailView(maLHP);
        Map<String, DiemEntryForm> submittedBySv = new HashMap<>();
        if (form.getEntries() != null) {
            for (DiemEntryForm entry : form.getEntries()) {
                if (entry != null && entry.getMaSV() != null) {
                    submittedBySv.put(entry.getMaSV(), entry);
                }
            }
        }
        List<DiemRow> mergedRows = diemService.getGradeSheet(maLHP).stream()
                .map(row -> {
                    DiemEntryForm submitted = submittedBySv.get(row.maSV());
                    if (submitted == null) {
                        return row;
                    }
                    boolean camThi = submitted.isCamThi();
                    return new DiemRow(
                            row.maSV(), row.hoTen(), row.maLopHC(),
                            camThi ? TrangThaiDangKy.CAM_THI : TrangThaiDangKy.HOP_LE,
                            submitted.getDiemQuaTrinh(), submitted.getDiemGiuaKy(),
                            camThi ? null : submitted.getDiemCuoiKy(),
                            row.diemTong(), row.diemHe4(), row.diemChu(), camThi, submitted.getLyDoCamThi()
                    );
                }).toList();
        model.addAttribute("lop", lhp);
        model.addAttribute("rows", mergedRows);
        model.addAttribute("form", form);
        model.addAttribute("flash", flash);
    }

    private void populateSelects(Model model) {
        model.addAttribute("monHocs", monHocService.findAll());
        model.addAttribute("giangViens", nhanSuService.findAllGiangVien());
        model.addAttribute("hocKys", hocKyService.findAll());
        model.addAttribute("statuses", TrangThaiLop.values());
    }

    private void populateFilters(Model model) {
        model.addAttribute("monHocs", monHocService.findAll());
        model.addAttribute("giangViens", nhanSuService.findAllGiangVien());
        model.addAttribute("hocKys", hocKyService.findAll());
        model.addAttribute("statuses", TrangThaiLop.values());
    }
}
