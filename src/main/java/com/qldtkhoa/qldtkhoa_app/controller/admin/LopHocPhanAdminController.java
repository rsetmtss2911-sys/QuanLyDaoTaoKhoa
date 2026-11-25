package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanForm;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/lop-hoc-phan")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class LopHocPhanAdminController {

    private final LopHocPhanService lopHocPhanService;
    private final MonHocService monHocService;
    private final HocKyService hocKyService;
    private final NhanSuService nhanSuService;

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
        LopHocPhan lhp = lopHocPhanService.getWithDetailsOrThrow(maLHP);
        model.addAttribute("lop", lhp);
        model.addAttribute("schedules", lopHocPhanService.findSchedules(maLHP));
        model.addAttribute("registrations", lopHocPhanService.findRegistrations(maLHP));
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
        if (result.hasErrors()) {
            form.setMaLHP(maLHP);
            populateSelects(model);
            model.addAttribute("isEdit", true);
            return "admin/lop-hoc-phan/form";
        }
        lopHocPhanService.update(maLHP, form);
        redirect.addFlashAttribute("flash", FlashMessage.success("lophocphan.updated", maLHP));
        return "redirect:/admin/lop-hoc-phan";
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
            model.addAttribute("lop", lopHocPhanService.getWithDetailsOrThrow(maLHP));
            model.addAttribute("schedules", lopHocPhanService.findSchedules(maLHP));
            model.addAttribute("registrations", lopHocPhanService.findRegistrations(maLHP));
            return "admin/lop-hoc-phan/detail";
        }
        try {
            lopHocPhanService.addSchedule(maLHP, form);
            redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.added"));
            return "redirect:/admin/lop-hoc-phan/" + maLHP;
        } catch (BusinessRuleException ex) {
            bindingResult.reject(ex.getMessageKey(), ex.getMessage());
            model.addAttribute("lop", lopHocPhanService.getWithDetailsOrThrow(maLHP));
            model.addAttribute("schedules", lopHocPhanService.findSchedules(maLHP));
            model.addAttribute("registrations", lopHocPhanService.findRegistrations(maLHP));
            return "admin/lop-hoc-phan/detail";
        }
    }

    @PostMapping("/{maLHP}/schedules/delete")
    public String removeSchedule(@PathVariable String maLHP,
                                 @RequestParam Integer thuTrongTuan,
                                 @RequestParam Integer tietBatDau,
                                 RedirectAttributes redirect) {
        lopHocPhanService.removeSchedule(maLHP, thuTrongTuan, tietBatDau);
        redirect.addFlashAttribute("flash", FlashMessage.success("lhp.schedule.deleted"));
        return "redirect:/admin/lop-hoc-phan/" + maLHP;
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
