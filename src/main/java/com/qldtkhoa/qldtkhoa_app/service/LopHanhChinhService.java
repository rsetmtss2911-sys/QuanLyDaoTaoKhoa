package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHanhChinhRow;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.GiangVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LopHanhChinhService {

    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final KhoaNhapHocRepository khoaNhapHocRepository;
    private final NganhHocRepository nganhHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;

    public Page<LopHanhChinhRow> search(String q, String maNganh, String maKhoaHoc, Pageable pageable) {
        return lopHanhChinhRepository.search(q == null ? "" : q.trim(), maNganh, maKhoaHoc, pageable)
                .map(this::toRow);
    }

    public List<LopHanhChinh> findAll() {
        return lopHanhChinhRepository.findAllByOrderByTenLopHCAsc();
    }

    public List<LopHanhChinhRow> findByCoVan(String maNS) {
        return lopHanhChinhRepository.findByCoVan_MaNS(maNS)
                .stream()
                .map(this::toRow)
                .toList();
    }

    public LopHanhChinh getOrThrow(String maLopHC) {
        return lopHanhChinhRepository.findById(maLopHC)
                .orElseThrow(() -> new ResourceNotFoundException("lophc.notFound", maLopHC));
    }

    public LopHanhChinh getWithDetailsOrThrow(String maLopHC) {
        return lopHanhChinhRepository.findByIdWithDetails(maLopHC)
                .orElseThrow(() -> new ResourceNotFoundException("lophc.notFound", maLopHC));
    }

    public LopHanhChinhForm getForm(String maLopHC) {
        LopHanhChinh lhc = getOrThrow(maLopHC);
        LopHanhChinhForm form = new LopHanhChinhForm();
        form.setMaLopHC(lhc.getMaLopHC());
        form.setTenLopHC(lhc.getTenLopHC());
        form.setMaKhoaHoc(lhc.getKhoaNhapHoc().getMaKhoaHoc());
        form.setMaNganh(lhc.getNganhHoc().getMaNganh());
        form.setMaCoVan(lhc.getCoVan() != null ? lhc.getCoVan().getMaNS() : null);
        return form;
    }

    @Transactional
    public void create(LopHanhChinhForm form) {
        String ma = form.getMaLopHC().trim();
        if (lopHanhChinhRepository.existsById(ma)) {
            throw new DuplicateResourceException("lophc.maLopHC.duplicate", ma);
        }

        KhoaNhapHoc knh = khoaNhapHocRepository.findById(form.getMaKhoaHoc())
                .orElseThrow(() -> new ResourceNotFoundException("khoanhaphoc.notFound", form.getMaKhoaHoc()));

        NganhHoc nganh = nganhHocRepository.findById(form.getMaNganh())
                .orElseThrow(() -> new ResourceNotFoundException("nganhhoc.notFound", form.getMaNganh()));

        GiangVien coVan = null;
        if (form.getMaCoVan() != null && !form.getMaCoVan().isBlank()) {
            coVan = giangVienRepository.findById(form.getMaCoVan())
                    .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", form.getMaCoVan()));
        }

        LopHanhChinh lhc = new LopHanhChinh(ma, form.getTenLopHC().trim(), nganh, knh, coVan);
        lopHanhChinhRepository.save(lhc);
    }

    @Transactional
    public void update(String maLopHC, LopHanhChinhForm form) {
        LopHanhChinh lhc = getOrThrow(maLopHC);
        lhc.setTenLopHC(form.getTenLopHC().trim());

        KhoaNhapHoc knh = khoaNhapHocRepository.findById(form.getMaKhoaHoc())
                .orElseThrow(() -> new ResourceNotFoundException("khoanhaphoc.notFound", form.getMaKhoaHoc()));
        lhc.setKhoaNhapHoc(knh);

        NganhHoc nganh = nganhHocRepository.findById(form.getMaNganh())
                .orElseThrow(() -> new ResourceNotFoundException("nganhhoc.notFound", form.getMaNganh()));
        lhc.setNganhHoc(nganh);

        if (form.getMaCoVan() != null && !form.getMaCoVan().isBlank()) {
            GiangVien coVan = giangVienRepository.findById(form.getMaCoVan())
                    .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", form.getMaCoVan()));
            lhc.setCoVan(coVan);
        } else {
            lhc.setCoVan(null);
        }
    }

    @Transactional
    public void delete(String maLopHC) {
        getOrThrow(maLopHC);
        if (sinhVienRepository.existsByLopHanhChinh_MaLopHC(maLopHC)) {
            throw new BusinessRuleException("lophc.delete.hasStudents", "Lớp hành chính đã có sinh viên, không thể xóa!");
        }
        lopHanhChinhRepository.deleteById(maLopHC);
    }

    public LopHanhChinhRow toRow(LopHanhChinh lhc) {
        long siSo = sinhVienRepository.countByLopHanhChinh_MaLopHC(lhc.getMaLopHC());
        String coVanName = (lhc.getCoVan() != null && lhc.getCoVan().getNhanSu() != null)
                ? lhc.getCoVan().getNhanSu().getHoTen()
                : null;
        String coVanMa = lhc.getCoVan() != null ? lhc.getCoVan().getMaNS() : null;

        return new LopHanhChinhRow(
                lhc.getMaLopHC(),
                lhc.getTenLopHC(),
                lhc.getKhoaNhapHoc().getMaKhoaHoc(),
                lhc.getKhoaNhapHoc().getNamBatDau(),
                lhc.getNganhHoc().getMaNganh(),
                lhc.getNganhHoc().getTenNganh(),
                coVanMa,
                coVanName,
                siSo
        );
    }
}
