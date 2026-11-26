package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhForm;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhSyncService;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/quyet-dinh")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','PHONG_DAO_TAO')")
public class QuyetDinhHocVuAdminController {

    private final QuyetDinhHocVuService quyetDinhHocVuService;
    private final QuyetDinhSyncService quyetDinhSyncService;
    private final Clock clock;

    @InitBinder("form")
    public void initFormBinder(WebDataBinder binder) {
        binder.setAllowedFields("maQuyetDinh", "maSV", "loaiQuyetDinh", "ngayHieuLuc", "lyDo");
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String maSV,
                       @PageableDefault(size = 20, sort = "ngayHieuLuc") Pageable pageable,
                       Model model) {
        model.addAttribute("page", quyetDinhHocVuService.search(q, maSV, SortWhitelist.of(pageable, "maQuyetDinh", "ngayHieuLuc")));
        model.addAttribute("q", q);
        model.addAttribute("maSV", maSV);
        return "admin/quyet-dinh/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String maSV, Model model) {
        QuyetDinhForm form = new QuyetDinhForm();
        form.setNgayHieuLuc(clock != null ? LocalDate.now(clock) : LocalDate.now());
        if (maSV != null && !maSV.isBlank()) {
            form.setMaSV(maSV);
        }
        model.addAttribute("form", form);
        model.addAttribute("loaiQuyetDinhs", LoaiQuyetDinh.values());
        return "admin/quyet-dinh/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") QuyetDinhForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                boolean scheduled = quyetDinhHocVuService.create(form);
                String key = scheduled ? "decision.scheduled" : "decision.issued";
                redirect.addFlashAttribute("flash", FlashMessage.success(key, form.getMaQuyetDinh()));
                return "redirect:/admin/quyet-dinh";
            } catch (DuplicateResourceException ex) {
                result.rejectValue("maQuyetDinh", ex.getMessageKey(), ex.getArgs(), null);
            } catch (ResourceNotFoundException ex) {
                result.rejectValue("maSV", "error.notFound", ex.getArgs(), "Không tìm thấy sinh viên");
            } catch (BusinessRuleException ex) {
                result.reject(ex.getMessageKey(), ex.getArgs(), ex.getMessageKey());
            }
        }
        model.addAttribute("loaiQuyetDinhs", LoaiQuyetDinh.values());
        return "admin/quyet-dinh/form";
    }

    @PostMapping("/sync-due")
    public String syncDueDecisions(RedirectAttributes redirect) {
        int count = quyetDinhSyncService.syncDueDecisions();
        redirect.addFlashAttribute("flash", FlashMessage.success("decision.sync.success", count));
        return "redirect:/admin/quyet-dinh";
    }
}
