package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.CTDTForm;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTRow;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.service.ChuongTrinhDaoTaoService;
import com.qldtkhoa.qldtkhoa_app.service.KhoaNhapHocService;
import com.qldtkhoa.qldtkhoa_app.service.MonHocService;
import com.qldtkhoa.qldtkhoa_app.service.NganhHocService;
import com.qldtkhoa.qldtkhoa_app.util.FlashMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/ctdt")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class ChuongTrinhDaoTaoController {

    private final ChuongTrinhDaoTaoService ctdtService;
    private final NganhHocService nganhHocService;
    private final KhoaNhapHocService khoaNhapHocService;
    private final MonHocService monHocService;

    @GetMapping
    public String index(@RequestParam(required = false) String maNganh,
                        @RequestParam(required = false) String maKhoaHoc,
                        Model model) {
        var nganhs = nganhHocService.findAll();
        var khoas = khoaNhapHocService.findAll();

        if ((maNganh == null || maNganh.isBlank()) && !nganhs.isEmpty()) {
            maNganh = nganhs.get(0).getMaNganh();
        }
        if ((maKhoaHoc == null || maKhoaHoc.isBlank()) && !khoas.isEmpty()) {
            maKhoaHoc = khoas.get(0).getMaKhoaHoc();
        }

        populateModel(maNganh, maKhoaHoc, model);

        CTDTForm form = new CTDTForm();
        form.setMaNganh(maNganh);
        form.setMaKhoaHoc(maKhoaHoc);
        model.addAttribute("form", form);

        return "admin/ctdt/index";
    }

    @PostMapping("/add")
    public String addCourse(@Valid @ModelAttribute("form") CTDTForm form,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                ctdtService.addCourse(form);
                redirect.addFlashAttribute("flash", FlashMessage.success("Đã thêm môn học vào chương trình đào tạo thành công!"));
                return "redirect:/admin/ctdt?maNganh=" + form.getMaNganh() + "&maKhoaHoc=" + form.getMaKhoaHoc();
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maMon", ex.getMessageKey(), "Môn học đã tồn tại trong CTĐT này.");
            }
        }
        populateModel(form.getMaNganh(), form.getMaKhoaHoc(), model);
        model.addAttribute("showAddModal", true);
        return "admin/ctdt/index";
    }

    private void populateModel(String maNganh, String maKhoaHoc, Model model) {
        var nganhs = nganhHocService.findAll();
        var khoas = khoaNhapHocService.findAll();

        List<CTDTRow> list = ctdtService.findByNganhAndKhoa(maNganh, maKhoaHoc);
        int tongTinChi = 0;
        int tcBatBuoc = 0;
        for (CTDTRow r : list) {
            int tc = r.soTinChi() != null ? r.soTinChi() : 0;
            tongTinChi += tc;
            if (r.loaiHocPhan() == LoaiHocPhan.BAT_BUOC) {
                tcBatBuoc += tc;
            }
        }
        int tcTuChon = tongTinChi - tcBatBuoc;

        model.addAttribute("nganhs", nganhs);
        model.addAttribute("khoas", khoas);
        model.addAttribute("allMonHocs", monHocService.findAll());
        model.addAttribute("currentNganh", maNganh);
        model.addAttribute("currentKhoa", maKhoaHoc);
        model.addAttribute("list", list);
        model.addAttribute("tongTinChi", tongTinChi);
        model.addAttribute("tcBatBuoc", tcBatBuoc);
        model.addAttribute("tcTuChon", tcTuChon);
    }

    @PostMapping("/delete")
    public String removeCourse(@RequestParam String maNganh,
                               @RequestParam String maKhoaHoc,
                               @RequestParam String maMon,
                               RedirectAttributes redirect) {
        ctdtService.removeCourse(maNganh, maKhoaHoc, maMon);
        redirect.addFlashAttribute("flash", FlashMessage.success("Đã xóa môn học khỏi chương trình đào tạo!"));
        return "redirect:/admin/ctdt?maNganh=" + maNganh + "&maKhoaHoc=" + maKhoaHoc;
    }
}
