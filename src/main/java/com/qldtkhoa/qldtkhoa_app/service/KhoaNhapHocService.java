package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaNhapHocForm;
import com.qldtkhoa.qldtkhoa_app.dto.KhoaNhapHocRow;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KhoaNhapHocService {

    private final KhoaNhapHocRepository khoaNhapHocRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final ChuongTrinhDaoTaoRepository ctdtRepository;

    public Page<KhoaNhapHocRow> search(String q, Pageable pageable) {
        Page<KhoaNhapHoc> page = khoaNhapHocRepository.search(q == null ? "" : q.trim(), pageable);
        return page.map(k -> new KhoaNhapHocRow(k.getMaKhoaHoc(), k.getNamBatDau()));
    }

    public List<KhoaNhapHoc> findAll() {
        return khoaNhapHocRepository.findAllByOrderByNamBatDauDesc();
    }

    public KhoaNhapHoc getOrThrow(String maKhoaHoc) {
        return khoaNhapHocRepository.findById(maKhoaHoc)
                .orElseThrow(() -> new ResourceNotFoundException("khoahoc.notFound", maKhoaHoc));
    }

    public KhoaNhapHocForm getForm(String maKhoaHoc) {
        KhoaNhapHoc k = getOrThrow(maKhoaHoc);
        KhoaNhapHocForm form = new KhoaNhapHocForm();
        form.setMaKhoaHoc(k.getMaKhoaHoc());
        form.setNamBatDau(k.getNamBatDau());
        return form;
    }

    @Transactional
    public void create(KhoaNhapHocForm form) {
        String ma = form.getMaKhoaHoc().trim();
        if (khoaNhapHocRepository.existsById(ma)) {
            throw new DuplicateResourceException("khoahoc.maKhoaHoc.duplicate", ma);
        }
        khoaNhapHocRepository.save(new KhoaNhapHoc(ma, form.getNamBatDau()));
    }

    @Transactional
    public void update(String maKhoaHoc, KhoaNhapHocForm form) {
        KhoaNhapHoc k = getOrThrow(maKhoaHoc);
        k.setNamBatDau(form.getNamBatDau());
    }

    @Transactional
    public void delete(String maKhoaHoc) {
        KhoaNhapHoc k = getOrThrow(maKhoaHoc);
        if (lopHanhChinhRepository.existsByKhoaNhapHoc_MaKhoaHoc(maKhoaHoc) || ctdtRepository.existsByKhoaNhapHoc_MaKhoaHoc(maKhoaHoc)) {
            throw new BusinessRuleException("khoahoc.delete.inUse", k.getMaKhoaHoc());
        }
        khoaNhapHocRepository.delete(k);
    }
}
