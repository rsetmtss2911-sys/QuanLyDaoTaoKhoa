package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyForm;
import com.qldtkhoa.qldtkhoa_app.dto.HocKyOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.HocKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.HocKyNamHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HocKyService {

    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final Clock clock;

    public Page<HocKyRow> search(String q, Pageable pageable) {
        Page<HocKyNamHoc> page = hocKyNamHocRepository.search(LikeUtils.escape(q), pageable);
        return page.map(hk -> new HocKyRow(
                hk.getMaHK(),
                hk.getTenHK(),
                hk.getNamHoc(),
                hk.getNgayBatDau(),
                hk.getNgayKetThuc()
        ));
    }

    public List<HocKyNamHoc> findAll() {
        return hocKyNamHocRepository.findAllByOrderByNamHocDescMaHKAsc();
    }

    public List<HocKyOptionRow> findAllOptions() {
        return findAll().stream().map(this::toOptionRow).toList();
    }

    public HocKyNamHoc getCurrentTerm() {
        LocalDate today = clock != null ? LocalDate.now(clock) : LocalDate.now();
        List<HocKyNamHoc> covering = hocKyNamHocRepository.findCoveringTerms(today);
        if (!covering.isEmpty()) {
            return covering.get(0);
        }
        List<HocKyNamHoc> started = hocKyNamHocRepository.findStartedBeforeOrOn(today);
        if (!started.isEmpty()) {
            return started.get(0);
        }
        return hocKyNamHocRepository.findTopByOrderByNamHocDescMaHKDesc().orElse(null);
    }

    public HocKyOptionRow getCurrentTermOption() {
        HocKyNamHoc current = getCurrentTerm();
        return current != null ? toOptionRow(current) : null;
    }

    private HocKyOptionRow toOptionRow(HocKyNamHoc hk) {
        return new HocKyOptionRow(hk.getMaHK(), hk.getTenHK(), hk.getNamHoc());
    }

    public HocKyNamHoc getOrThrow(String maHK) {
        return hocKyNamHocRepository.findById(maHK)
                .orElseThrow(() -> new ResourceNotFoundException("hocky.notFound", maHK));
    }

    public HocKyForm getForm(String maHK) {
        HocKyNamHoc hk = getOrThrow(maHK);
        HocKyForm form = new HocKyForm();
        form.setMaHK(hk.getMaHK());
        form.setTenHK(hk.getTenHK());
        form.setNamHoc(hk.getNamHoc());
        form.setNgayBatDau(hk.getNgayBatDau());
        form.setNgayKetThuc(hk.getNgayKetThuc());
        form.setNgayMoDangKy(hk.getNgayMoDangKy());
        form.setNgayDongDangKy(hk.getNgayDongDangKy());
        form.setHanRutHocPhan(hk.getHanRutHocPhan());
        form.setHanKhoaDiem(hk.getHanKhoaDiem());
        return form;
    }

    @Transactional
    public void create(HocKyForm form) {
        String ma = form.getMaHK().trim();
        if (hocKyNamHocRepository.existsById(ma)) {
            throw new DuplicateResourceException("hocky.maHK.duplicate", ma);
        }
        validateDates(null, form);
        HocKyNamHoc hk = new HocKyNamHoc(
                ma, form.getTenHK().trim(), form.getNamHoc(), form.getNgayBatDau(), form.getNgayKetThuc()
        );
        applyMilestones(hk, form);
        hocKyNamHocRepository.save(hk);
    }

    @Transactional
    public void update(String maHK, HocKyForm form) {
        HocKyNamHoc hk = getOrThrow(maHK);
        validateDates(maHK, form);
        hk.setTenHK(form.getTenHK().trim());
        hk.setNamHoc(form.getNamHoc());
        hk.setNgayBatDau(form.getNgayBatDau());
        hk.setNgayKetThuc(form.getNgayKetThuc());
        applyMilestones(hk, form);
    }

    @Transactional
    public void delete(String maHK) {
        HocKyNamHoc hk = getOrThrow(maHK);
        if (lopHocPhanRepository.existsByHocKy_MaHK(maHK)) {
            throw new BusinessRuleException("hocky.delete.inUse", hk.getTenHK());
        }
        hocKyNamHocRepository.delete(hk);
    }

    private void applyMilestones(HocKyNamHoc hk, HocKyForm form) {
        hk.setNgayMoDangKy(form.getNgayMoDangKy());
        hk.setNgayDongDangKy(form.getNgayDongDangKy());
        hk.setHanRutHocPhan(form.getHanRutHocPhan());
        hk.setHanKhoaDiem(form.getHanKhoaDiem());
    }

    private void validateDates(String excludeMaHK, HocKyForm form) {
        LocalDate start = form.getNgayBatDau();
        LocalDate end = form.getNgayKetThuc();
        if (start == null || end == null) {
            throw new BusinessRuleException("hocky.dateRequired");
        }
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("hocky.dateOrder");
        }
        if (hocKyNamHocRepository.existsOverlappingTerm(excludeMaHK, start, end)) {
            throw new BusinessRuleException("hocky.dateOverlap");
        }
        LocalDate regOpen = form.getNgayMoDangKy();
        LocalDate regClose = form.getNgayDongDangKy();
        LocalDate withdrawDeadline = form.getHanRutHocPhan();
        LocalDate gradeLock = form.getHanKhoaDiem();

        if (regOpen != null && regClose != null && regClose.isBefore(regOpen)) {
            throw new BusinessRuleException("hocky.milestoneOrder");
        }
        if (regOpen != null && withdrawDeadline != null && withdrawDeadline.isBefore(regOpen)) {
            throw new BusinessRuleException("hocky.milestoneOrder");
        }
        if (regClose != null && withdrawDeadline != null && withdrawDeadline.isBefore(regClose)) {
            throw new BusinessRuleException("hocky.milestoneOrder");
        }
        if (gradeLock != null && gradeLock.isBefore(start)) {
            throw new BusinessRuleException("hocky.gradeLockBeforeStart");
        }
    }
}
