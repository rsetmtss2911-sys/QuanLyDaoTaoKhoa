package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.NganhHocForm;
import com.qldtkhoa.qldtkhoa_app.dto.NganhHocRow;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NganhHocService {

    private final NganhHocRepository nganhHocRepository;
    private final KhoaRepository khoaRepository;
    private final ChuongTrinhDaoTaoRepository ctdtRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;

    public Page<NganhHocRow> search(String q, String maKhoa, Pageable pageable) {
        Page<NganhHoc> page = nganhHocRepository.search(LikeUtils.escape(q), maKhoa, pageable);
        return page.map(n -> new NganhHocRow(
                n.getMaNganh(),
                n.getTenNganh(),
                n.getKhoa().getMaKhoa(),
                n.getKhoa().getTenKhoa()
        ));
    }

    public List<NganhHoc> findByKhoa(String maKhoa) {
        return nganhHocRepository.findByKhoa_MaKhoaOrderByTenNganh(maKhoa);
    }

    public List<NganhHoc> findAll() {
        return nganhHocRepository.findAllByOrderByTenNganhAsc();
    }

    public NganhHoc getOrThrow(String maNganh) {
        return nganhHocRepository.findById(maNganh)
                .orElseThrow(() -> new ResourceNotFoundException("nganh.notFound", maNganh));
    }

    public NganhHocForm getForm(String maNganh) {
        NganhHoc n = getOrThrow(maNganh);
        NganhHocForm form = new NganhHocForm();
        form.setMaNganh(n.getMaNganh());
        form.setTenNganh(n.getTenNganh());
        form.setMaKhoa(n.getKhoa().getMaKhoa());
        return form;
    }

    @Transactional
    public void create(NganhHocForm form) {
        String ma = form.getMaNganh().trim();
        if (nganhHocRepository.existsById(ma)) {
            throw new DuplicateResourceException("nganh.maNganh.duplicate", ma);
        }
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("khoa.notFound", form.getMaKhoa()));
        nganhHocRepository.save(new NganhHoc(ma, form.getTenNganh().trim(), khoa));
    }

    @Transactional
    public void update(String maNganh, NganhHocForm form) {
        NganhHoc n = getOrThrow(maNganh);
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("khoa.notFound", form.getMaKhoa()));
        n.setTenNganh(form.getTenNganh().trim());
        n.setKhoa(khoa);
    }

    @Transactional
    public void delete(String maNganh) {
        NganhHoc n = getOrThrow(maNganh);
        if (ctdtRepository.existsByNganhHoc_MaNganh(maNganh) || lopHanhChinhRepository.existsByNganhHoc_MaNganh(maNganh)) {
            throw new BusinessRuleException("nganh.delete.inUse", n.getTenNganh());
        }
        nganhHocRepository.delete(n);
    }
}
