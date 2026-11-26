package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienForm;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.VaiTroRepository;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SinhVienService {

    private final SinhVienRepository sinhVienRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public Page<SinhVienRow> search(String q, String maLopHC, TrangThaiSinhVien status, Pageable pageable) {
        return sinhVienRepository.search(LikeUtils.escape(q), maLopHC, status, pageable)
                .map(this::toRow);
    }

    public List<SinhVienRow> findByLopHanhChinh(String maLopHC) {
        return sinhVienRepository.findByLopHanhChinh_MaLopHC(maLopHC)
                .stream()
                .map(this::toRow)
                .toList();
    }

    public SinhVien getOrThrow(String maSV) {
        return sinhVienRepository.findById(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));
    }

    public SinhVien getWithDetailsOrThrow(String maSV) {
        return sinhVienRepository.findByIdWithDetails(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));
    }

    public SinhVien findByEmailOrThrow(String email) {
        return sinhVienRepository.findByTaiKhoan_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", email));
    }

    public SinhVienForm getForm(String maSV) {
        SinhVien sv = getWithDetailsOrThrow(maSV);
        SinhVienForm form = new SinhVienForm();
        form.setMaSV(sv.getMaSV());
        form.setHoTen(sv.getHoTen());
        form.setNgaySinh(sv.getNgaySinh());
        form.setGioiTinh(sv.getGioiTinh() != null ? sv.getGioiTinh() : GioiTinh.NAM);
        form.setMaLopHC(sv.getLopHanhChinh() != null ? sv.getLopHanhChinh().getMaLopHC() : "");
        form.setEmail(sv.getTaiKhoan() != null ? sv.getTaiKhoan().getEmail() : "");
        return form;
    }

    @Transactional
    public void create(SinhVienForm form) {
        String ma = form.getMaSV().trim();
        String email = form.getEmail().trim().toLowerCase();

        if (sinhVienRepository.existsById(ma)) {
            throw new DuplicateResourceException("sinhvien.maSV.duplicate", ma);
        }
        if (taiKhoanRepository.existsById(email)) {
            throw new DuplicateResourceException("sinhvien.email.duplicate", email);
        }
        validateAge(form.getNgaySinh());

        LopHanhChinh lhc = lopHanhChinhRepository.findById(form.getMaLopHC())
                .orElseThrow(() -> new ResourceNotFoundException("lophc.notFound", form.getMaLopHC()));

        VaiTro roleSv = vaiTroRepository.findById("SINH_VIEN")
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound", "SINH_VIEN"));

        String rawPassword = (form.getMatKhau() != null && !form.getMatKhau().isBlank())
                ? form.getMatKhau().trim()
                : "123456";

        TaiKhoan tk = new TaiKhoan(email, passwordEncoder.encode(rawPassword), roleSv, true);
        taiKhoanRepository.save(tk);

        SinhVien sv = new SinhVien(
                ma,
                form.getHoTen().trim(),
                form.getNgaySinh(),
                form.getGioiTinh(),
                tk,
                TrangThaiSinhVien.DANG_HOC,
                lhc
        );
        sinhVienRepository.save(sv);
    }

    @Transactional
    public void update(String maSV, SinhVienForm form) {
        SinhVien sv = getOrThrow(maSV);
        validateAge(form.getNgaySinh());
        sv.setHoTen(form.getHoTen().trim());
        sv.setNgaySinh(form.getNgaySinh());
        sv.setGioiTinh(form.getGioiTinh());

        LopHanhChinh lhc = lopHanhChinhRepository.findById(form.getMaLopHC())
                .orElseThrow(() -> new ResourceNotFoundException("lophc.notFound", form.getMaLopHC()));
        sv.setLopHanhChinh(lhc);
    }

    @Transactional
    public void delete(String maSV) {
        getOrThrow(maSV);
        throw new BusinessRuleException("sinhvien.delete.forbidden");
    }

    private void validateAge(LocalDate ngaySinh) {
        if (ngaySinh != null) {
            LocalDate today = clock != null ? LocalDate.now(clock) : LocalDate.now();
            if (ngaySinh.isAfter(today.minusYears(17))) {
                throw new BusinessRuleException("sinhvien.ngaySinh.age");
            }
        }
    }

    public SinhVienRow toRow(SinhVien sv) {
        String email = sv.getTaiKhoan() != null ? sv.getTaiKhoan().getEmail() : "";
        String maLopHC = sv.getLopHanhChinh() != null ? sv.getLopHanhChinh().getMaLopHC() : "";
        String tenLopHC = sv.getLopHanhChinh() != null ? sv.getLopHanhChinh().getTenLopHC() : "";
        return new SinhVienRow(
                sv.getMaSV(),
                sv.getHoTen(),
                email,
                sv.getGioiTinh(),
                sv.getNgaySinh(),
                maLopHC,
                tenLopHC,
                sv.getTrangThaiHienTai()
        );
    }
}
