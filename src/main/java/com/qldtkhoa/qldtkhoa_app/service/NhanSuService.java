package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.GiangVienOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.NhanSuForm;
import com.qldtkhoa.qldtkhoa_app.dto.NhanSuRow;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NhanSuService {

    private final NhanSuRepository nhanSuRepository;
    private final GiangVienRepository giangVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final BoMonRepository boMonRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionInvalidationHelper sessionInvalidationHelper;

    public Page<NhanSuRow> search(String q, String role, Pageable pageable) {
        return nhanSuRepository.searchRows(LikeUtils.escape(q), role, pageable);
    }

    public List<GiangVien> findAllGiangVien() {
        return giangVienRepository.findAllByOrderByNhanSu_HoTenAsc();
    }

    public List<GiangVienOptionRow> findAllGiangVienOptions() {
        return findAllGiangVien().stream()
                .map(gv -> new GiangVienOptionRow(
                        gv.getMaNS(),
                        gv.getNhanSu() != null ? gv.getNhanSu().getHoTen() : gv.getMaNS(),
                        gv.getBoMon() != null ? gv.getBoMon().getTenBoMon() : ""
                ))
                .toList();
    }

    public NhanSu getOrThrow(String maNS) {
        return nhanSuRepository.findById(maNS)
                .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", maNS));
    }

    public NhanSuForm getForm(String maNS) {
        NhanSu ns = getOrThrow(maNS);
        NhanSuForm form = new NhanSuForm();
        form.setMaNS(ns.getMaNS());
        form.setHoTen(ns.getHoTen());
        form.setEmail(ns.getTaiKhoan().getEmail());
        form.setMaVaiTro(ns.getTaiKhoan().getVaiTro().getMaVaiTro());

        giangVienRepository.findById(maNS).ifPresent(gv -> {
            form.setHocHam(gv.getHocHam());
            form.setHocVi(gv.getHocVi());
            form.setMaBoMon(gv.getBoMon().getMaBoMon());
        });

        return form;
    }

    @Transactional
    public void create(NhanSuForm form) {
        String ma = form.getMaNS().trim();
        String email = form.getEmail().trim().toLowerCase();

        if ("SINH_VIEN".equalsIgnoreCase(form.getMaVaiTro())) {
            throw new BusinessRuleException("nhansu.vaiTro.invalid");
        }
        if (nhanSuRepository.existsById(ma)) {
            throw new DuplicateResourceException("nhansu.maNS.duplicate", ma);
        }
        if (taiKhoanRepository.existsById(email)) {
            throw new DuplicateResourceException("nhansu.email.duplicate", email);
        }

        VaiTro vaiTro = vaiTroRepository.findById(form.getMaVaiTro())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound", form.getMaVaiTro()));

        if ("GIANG_VIEN".equalsIgnoreCase(form.getMaVaiTro())) {
            validateLecturerFields(form);
        }

        String rawPassword = (form.getMatKhau() != null && !form.getMatKhau().isBlank()) ? form.getMatKhau().trim() : "123456";
        TaiKhoan tk = new TaiKhoan(email, passwordEncoder.encode(rawPassword), vaiTro, true);
        taiKhoanRepository.save(tk);

        NhanSu ns = new NhanSu(ma, form.getHoTen().trim(), tk);
        nhanSuRepository.save(ns);

        if ("GIANG_VIEN".equalsIgnoreCase(form.getMaVaiTro())) {
            BoMon bm = boMonRepository.findById(form.getMaBoMon())
                    .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", form.getMaBoMon()));
            GiangVien gv = new GiangVien(ns, form.getHocHam(), form.getHocVi().trim(), bm);
            giangVienRepository.save(gv);
        }
    }

    @Transactional
    public void update(String maNS, NhanSuForm form) {
        NhanSu ns = getOrThrow(maNS);
        ns.setHoTen(form.getHoTen().trim());

        if ("GIANG_VIEN".equalsIgnoreCase(ns.getTaiKhoan().getVaiTro().getMaVaiTro())) {
            validateLecturerFields(form);
            BoMon bm = boMonRepository.findById(form.getMaBoMon())
                    .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", form.getMaBoMon()));

            GiangVien gv = giangVienRepository.findById(maNS)
                    .orElseGet(() -> new GiangVien(ns, form.getHocHam(), form.getHocVi().trim(), bm));
            gv.setHocHam(form.getHocHam());
            gv.setHocVi(form.getHocVi().trim());
            gv.setBoMon(bm);
            giangVienRepository.save(gv);
        }
    }

    @Transactional
    public void delete(String maNS) {
        delete(maNS, null);
    }

    @Transactional
    public void delete(String maNS, String currentAdminEmail) {
        NhanSu ns = getOrThrow(maNS);
        TaiKhoan tk = ns.getTaiKhoan();

        if (tk != null && currentAdminEmail != null && tk.getEmail().equalsIgnoreCase(currentAdminEmail.trim())) {
            throw new BusinessRuleException("nhansu.error.selfDelete");
        }
        if (tk != null
                && tk.getVaiTro() != null
                && "ADMIN".equalsIgnoreCase(tk.getVaiTro().getMaVaiTro())
                && Boolean.TRUE.equals(tk.getTrangThaiKichHoat())
                && taiKhoanRepository.countByVaiTro_MaVaiTroAndTrangThaiKichHoat("ADMIN", true) <= 1) {
            throw new BusinessRuleException("nhansu.error.lastAdmin");
        }

        if (lopHanhChinhRepository.existsByCoVan_MaNS(maNS)) {
            throw new BusinessRuleException("Nhân sự này đang được phân công làm Cố vấn học tập, không thể xóa!");
        }
        if (lopHocPhanRepository.existsByGiangVien_MaNS(maNS)) {
            throw new BusinessRuleException("Giảng viên này đang phụ trách lớp học phần, không thể xóa!");
        }

        giangVienRepository.deleteById(maNS);
        nhanSuRepository.delete(ns);
        if (tk != null) {
            String email = tk.getEmail();
            taiKhoanRepository.delete(tk);
            taiKhoanRepository.flush();
            sessionInvalidationHelper.expireUserSessions(email);
        }
    }

    private void validateLecturerFields(NhanSuForm form) {
        if (form.getMaBoMon() == null || form.getMaBoMon().isBlank()) {
            throw new BusinessRuleException("nhansu.boMon.required");
        }
        if (form.getHocVi() == null || form.getHocVi().isBlank()) {
            throw new BusinessRuleException("nhansu.hocVi.required");
        }
    }
}
