package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyForm;
import com.qldtkhoa.qldtkhoa_app.dto.HocKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.HocKyNamHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HocKyService {

    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public Page<HocKyRow> search(String q, Pageable pageable) {
        Page<HocKyNamHoc> page = hocKyNamHocRepository.search(q == null ? "" : q.trim(), pageable);
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

    public HocKyNamHoc getCurrentTerm() {
        return hocKyNamHocRepository.findTopByOrderByNamHocDescMaHKDesc()
                .orElse(null);
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
        return form;
    }

    @Transactional
    public void create(HocKyForm form) {
        String ma = form.getMaHK().trim();
        if (hocKyNamHocRepository.existsById(ma)) {
            throw new DuplicateResourceException("hocky.maHK.duplicate", ma);
        }
        validateDates(form);
        hocKyNamHocRepository.save(new HocKyNamHoc(
                ma, form.getTenHK().trim(), form.getNamHoc(), form.getNgayBatDau(), form.getNgayKetThuc()
        ));
    }

    @Transactional
    public void update(String maHK, HocKyForm form) {
        HocKyNamHoc hk = getOrThrow(maHK);
        validateDates(form);
        hk.setTenHK(form.getTenHK().trim());
        hk.setNamHoc(form.getNamHoc());
        hk.setNgayBatDau(form.getNgayBatDau());
        hk.setNgayKetThuc(form.getNgayKetThuc());
    }

    @Transactional
    public void delete(String maHK) {
        HocKyNamHoc hk = getOrThrow(maHK);
        if (lopHocPhanRepository.existsByHocKy_MaHK(maHK)) {
            throw new BusinessRuleException("hocky.delete.inUse", hk.getTenHK());
        }
        hocKyNamHocRepository.delete(hk);
    }

    private void validateDates(HocKyForm form) {
        if (form.getNgayBatDau() != null && form.getNgayKetThuc() != null) {
            if (!form.getNgayKetThuc().isAfter(form.getNgayBatDau())) {
                throw new BusinessRuleException("hocky.dateOrder");
            }
        }
    }
}
