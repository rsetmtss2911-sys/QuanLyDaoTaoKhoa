package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.service.AuditService;
import com.qldtkhoa.qldtkhoa_app.util.SortWhitelist;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/nhat-ky")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NhatKyHeThongController {

    private final AuditService auditService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) String doiTuong,
                       @RequestParam(required = false) String hanhDong,
                       @PageableDefault(size = 20, sort = "thoiGian", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", auditService.search(q, doiTuong, hanhDong,
                SortWhitelist.of(pageable, "thoiGian", "maNhatKy", "hanhDong", "doiTuong")));
        model.addAttribute("doiTuongs", auditService.findDistinctDoiTuong());
        model.addAttribute("hanhDongs", auditService.findDistinctHanhDong());
        model.addAttribute("q", q);
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("hanhDong", hanhDong);
        return "admin/nhat-ky/list";
    }
}
