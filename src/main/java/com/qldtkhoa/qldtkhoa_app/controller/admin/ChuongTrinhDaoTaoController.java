package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.CTDTForm;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTRow;
import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/ctdt")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class ChuongTrinhDaoTaoController {

    private final ChuongTrinhDaoTaoService ctdtService;
    private final NganhHocService nganhHocService;
    private final KhoaNhapHocService khoaNhapHocService;
    private final MonHocService monHocService;

    @InitBinder("form")
    public void initFormBinder(WebDataBinder binder) {
        binder.setAllowedFields("maNganh", "maKhoaHoc", "maMon", "khoiKienThuc", "loaiHocPhan", "hocKyChuan");
    }

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
                redirect.addFlashAttribute("flash", FlashMessage.success("ctdt.added"));
                redirect.addAttribute("maNganh", form.getMaNganh());
                redirect.addAttribute("maKhoaHoc", form.getMaKhoaHoc());
                return "redirect:/admin/ctdt";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maMon", ex.getMessageKey(), "Môn học đã tồn tại trong CTĐT này.");
            }
        }
        populateModel(form.getMaNganh(), form.getMaKhoaHoc(), model);
        model.addAttribute("showAddModal", true);
        return "admin/ctdt/index";
    }

    @PostMapping("/update")
    public String updateCourse(@Valid @ModelAttribute("form") CTDTForm form,
                               BindingResult result,
                               RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("flash", FlashMessage.danger("ctdt.update.invalid"));
        } else {
            ctdtService.updateCourse(form);
            redirect.addFlashAttribute("flash", FlashMessage.success("ctdt.updated"));
        }
        redirect.addAttribute("maNganh", form.getMaNganh());
        redirect.addAttribute("maKhoaHoc", form.getMaKhoaHoc());
        return "redirect:/admin/ctdt";
    }

    @PostMapping("/quotas")
    public String saveQuotas(@RequestParam String maNganh,
                             @RequestParam String maKhoaHoc,
                             @RequestParam(defaultValue = "0") Integer daiCuong,
                             @RequestParam(defaultValue = "0") Integer coSoNganh,
                             @RequestParam(defaultValue = "0") Integer chuyenNganh,
                             RedirectAttributes redirect) {
        try {
            ctdtService.saveQuotas(maNganh, maKhoaHoc, daiCuong, coSoNganh, chuyenNganh);
            redirect.addFlashAttribute("flash", FlashMessage.success("ctdt.quota.saved"));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maNganh", maNganh);
        redirect.addAttribute("maKhoaHoc", maKhoaHoc);
        return "redirect:/admin/ctdt";
    }

    @PostMapping("/clone")
    public String cloneCurriculum(@RequestParam String maNganh,
                                  @RequestParam String sourceKhoaHoc,
                                  @RequestParam String targetKhoaHoc,
                                  RedirectAttributes redirect) {
        try {
            int copied = ctdtService.cloneCurriculum(maNganh, sourceKhoaHoc, targetKhoaHoc);
            redirect.addFlashAttribute("flash", FlashMessage.success("ctdt.clone.success", copied, sourceKhoaHoc, targetKhoaHoc));
        } catch (BusinessRuleException ex) {
            redirect.addFlashAttribute("flash", FlashMessage.danger(ex.getMessageKey(), ex.getArgs()));
        }
        redirect.addAttribute("maNganh", maNganh);
        redirect.addAttribute("maKhoaHoc", targetKhoaHoc);
        return "redirect:/admin/ctdt";
    }

    @PostMapping("/delete")
    public String removeCourse(@RequestParam String maNganh,
                               @RequestParam String maKhoaHoc,
                               @RequestParam String maMon,
                               RedirectAttributes redirect) {
        ctdtService.removeCourse(maNganh, maKhoaHoc, maMon);
        redirect.addFlashAttribute("flash", FlashMessage.success("ctdt.removed"));
        redirect.addAttribute("maNganh", maNganh);
        redirect.addAttribute("maKhoaHoc", maKhoaHoc);
        return "redirect:/admin/ctdt";
    }

    private void populateModel(String maNganh, String maKhoaHoc, Model model) {
        var nganhs = nganhHocService.findAll();
        var khoas = khoaNhapHocService.findAll();

        List<CTDTRow> list = ctdtService.findByNganhAndKhoa(maNganh, maKhoaHoc);
        Map<KhoiKienThuc, Integer> quotas = ctdtService.getQuotas(maNganh, maKhoaHoc);
        int tongTinChiBatBuoc = 0;
        int tcTuChonDanhMuc = 0;
        for (CTDTRow r : list) {
            int tc = r.soTinChi() != null ? r.soTinChi() : 0;
            if (r.loaiHocPhan() == LoaiHocPhan.BAT_BUOC) {
                tongTinChiBatBuoc += tc;
            } else {
                tcTuChonDanhMuc += tc;
            }
        }
        int dinhMucTuChon = quotas.values().stream().mapToInt(v -> v != null ? v : 0).sum();
        int tcTuChon = dinhMucTuChon > 0 ? dinhMucTuChon : tcTuChonDanhMuc;
        int tongTinChi = tongTinChiBatBuoc + tcTuChon;

        model.addAttribute("nganhs", nganhs);
        model.addAttribute("khoas", khoas);
        model.addAttribute("allMonHocs", monHocService.findAll());
        model.addAttribute("currentNganh", maNganh);
        model.addAttribute("currentKhoa", maKhoaHoc);
        model.addAttribute("list", list);
        model.addAttribute("quotas", quotas);
        model.addAttribute("loaiHocPhans", LoaiHocPhan.values());
        model.addAttribute("khoiKienThucs", KhoiKienThuc.values());
        model.addAttribute("quotaDaiCuong", quotas.getOrDefault(KhoiKienThuc.DAI_CUONG, 0));
        model.addAttribute("quotaCoSoNganh", quotas.getOrDefault(KhoiKienThuc.CO_SO_NGANH, 0));
        model.addAttribute("quotaChuyenNganh", quotas.getOrDefault(KhoiKienThuc.CHUYEN_NGANH, 0));
        model.addAttribute("tongTinChi", tongTinChi);
        model.addAttribute("tcBatBuoc", tongTinChiBatBuoc);
        model.addAttribute("tcTuChon", tcTuChon);
    }
}
