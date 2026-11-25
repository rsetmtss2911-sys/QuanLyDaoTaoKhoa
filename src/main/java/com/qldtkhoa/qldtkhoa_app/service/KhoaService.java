package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaForm;
import com.qldtkhoa.qldtkhoa_app.dto.KhoaRow;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.BoMonRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KhoaService {

    private final KhoaRepository khoaRepository;
    private final BoMonRepository boMonRepository;
    private final NganhHocRepository nganhHocRepository;

    public Page<KhoaRow> search(String q, Pageable pageable) {
        Page<Khoa> page = khoaRepository.search(q == null ? "" : q.trim(), pageable);
        return page.map(k -> new KhoaRow(
                k.getMaKhoa(),
                k.getTenKhoa(),
                k.getNamThanhLap(),
                boMonRepository.countByKhoa_MaKhoa(k.getMaKhoa()),
                nganhHocRepository.countByKhoa_MaKhoa(k.getMaKhoa())
        ));
    }

    public List<Khoa> findAll() {
        return khoaRepository.findAllByOrderByTenKhoaAsc();
    }

    public Khoa getOrThrow(String maKhoa) {
        return khoaRepository.findById(maKhoa)
                .orElseThrow(() -> new ResourceNotFoundException("khoa.notFound", maKhoa));
    }

    public KhoaForm getForm(String maKhoa) {
        Khoa k = getOrThrow(maKhoa);
        KhoaForm form = new KhoaForm();
        form.setMaKhoa(k.getMaKhoa());
        form.setTenKhoa(k.getTenKhoa());
        form.setNamThanhLap(k.getNamThanhLap());
        return form;
    }

    @Transactional
    public void create(KhoaForm form) {
        String ma = form.getMaKhoa().trim();
        if (khoaRepository.existsById(ma)) {
            throw new DuplicateResourceException("khoa.maKhoa.duplicate", ma);
        }
        khoaRepository.save(new Khoa(ma, form.getTenKhoa().trim(), form.getNamThanhLap()));
    }

    @Transactional
    public void update(String maKhoa, KhoaForm form) {
        Khoa k = getOrThrow(maKhoa);
        k.setTenKhoa(form.getTenKhoa().trim());
        k.setNamThanhLap(form.getNamThanhLap());
    }

    @Transactional
    public void delete(String maKhoa) {
        Khoa k = getOrThrow(maKhoa);
        if (boMonRepository.existsByKhoa_MaKhoa(maKhoa) || nganhHocRepository.existsByKhoa_MaKhoa(maKhoa)) {
            throw new BusinessRuleException("khoa.delete.inUse", k.getTenKhoa());
        }
        khoaRepository.delete(k);
    }
}
