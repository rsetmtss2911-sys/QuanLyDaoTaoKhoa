package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.BoMonForm;
import com.qldtkhoa.qldtkhoa_app.dto.BoMonRow;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.GiangVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
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
public class BoMonService {

    private final BoMonRepository boMonRepository;
    private final KhoaRepository khoaRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;

    public Page<BoMonRow> search(String q, String maKhoa, Pageable pageable) {
        Page<BoMon> page = boMonRepository.search(LikeUtils.escape(q), maKhoa, pageable);
        return page.map(b -> new BoMonRow(
                b.getMaBoMon(),
                b.getTenBoMon(),
                b.getKhoa().getMaKhoa(),
                b.getKhoa().getTenKhoa()
        ));
    }

    public List<BoMon> findByKhoa(String maKhoa) {
        return boMonRepository.findByKhoa_MaKhoaOrderByTenBoMon(maKhoa);
    }

    public List<BoMon> findAll() {
        return boMonRepository.findAllByOrderByTenBoMonAsc();
    }

    public BoMon getOrThrow(String maBoMon) {
        return boMonRepository.findById(maBoMon)
                .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", maBoMon));
    }

    public BoMonForm getForm(String maBoMon) {
        BoMon bm = getOrThrow(maBoMon);
        BoMonForm form = new BoMonForm();
        form.setMaBoMon(bm.getMaBoMon());
        form.setTenBoMon(bm.getTenBoMon());
        form.setMaKhoa(bm.getKhoa().getMaKhoa());
        return form;
    }

    @Transactional
    public void create(BoMonForm form) {
        String ma = form.getMaBoMon().trim();
        if (boMonRepository.existsById(ma)) {
            throw new DuplicateResourceException("bomon.maBoMon.duplicate", ma);
        }
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("khoa.notFound", form.getMaKhoa()));
        boMonRepository.save(new BoMon(ma, form.getTenBoMon().trim(), khoa));
    }

    @Transactional
    public void update(String maBoMon, BoMonForm form) {
        BoMon bm = getOrThrow(maBoMon);
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new ResourceNotFoundException("khoa.notFound", form.getMaKhoa()));
        bm.setTenBoMon(form.getTenBoMon().trim());
        bm.setKhoa(khoa);
    }

    @Transactional
    public void delete(String maBoMon) {
        BoMon bm = getOrThrow(maBoMon);
        if (giangVienRepository.existsByBoMon_MaBoMon(maBoMon) || monHocRepository.existsByBoMon_MaBoMon(maBoMon)) {
            throw new BusinessRuleException("bomon.delete.inUse", bm.getTenBoMon());
        }
        boMonRepository.delete(bm);
    }
}
