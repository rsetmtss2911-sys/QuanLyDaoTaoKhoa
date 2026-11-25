package com.qldtkhoa.qldtkhoa_app.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Page<NhanSuRow> search(String q, String role, Pageable pageable) {
        Page<NhanSu> page = nhanSuRepository.search(q == null ? "" : q.trim(), role, pageable);
        return page.map(ns -> {
            Optional<GiangVien> gvOpt = giangVienRepository.findById(ns.getMaNS());
            boolean isGv = gvOpt.isPresent();
            String hocHam = isGv ? gvOpt.get().getHocHam() : null;
            String hocVi = isGv ? gvOpt.get().getHocVi() : null;
            String tenBoMon = isGv ? gvOpt.get().getBoMon().getTenBoMon() : null;

            return new NhanSuRow(
                    ns.getMaNS(),
                    ns.getHoTen(),
                    ns.getTaiKhoan().getEmail(),
                    ns.getTaiKhoan().getVaiTro().getMaVaiTro(),
                    ns.getTaiKhoan().getVaiTro().getTenVaiTro(),
                    ns.getTaiKhoan().getTrangThaiKichHoat(),
                    isGv,
                    hocHam,
                    hocVi,
                    tenBoMon
            );
        });
    }

    public List<GiangVien> findAllGiangVien() {
        return giangVienRepository.findAllByOrderByNhanSu_HoTenAsc();
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

        if (nhanSuRepository.existsById(ma)) {
            throw new DuplicateResourceException("nhansu.maNS.duplicate", ma);
        }
        if (taiKhoanRepository.existsById(email)) {
            throw new DuplicateResourceException("nhansu.email.duplicate", email);
        }

        VaiTro vaiTro = vaiTroRepository.findById(form.getMaVaiTro())
                .orElseThrow(() -> new ResourceNotFoundException("error.notFound", form.getMaVaiTro()));

        if ("GIANG_VIEN".equalsIgnoreCase(form.getMaVaiTro())) {
            if (form.getMaBoMon() == null || form.getMaBoMon().isBlank()) {
                throw new BusinessRuleException("nhansu.boMon.required");
            }
            if (form.getHocVi() == null || form.getHocVi().isBlank()) {
                throw new BusinessRuleException("nhansu.hocVi.required");
            }
        }

        // 1. Tạo Tài khoản
        String rawPassword = (form.getMatKhau() != null && !form.getMatKhau().isBlank()) ? form.getMatKhau().trim() : "123456";
        TaiKhoan tk = new TaiKhoan(email, passwordEncoder.encode(rawPassword), vaiTro, true);
        taiKhoanRepository.save(tk);

        // 2. Tạo Nhân sự
        NhanSu ns = new NhanSu(ma, form.getHoTen().trim(), tk);
        nhanSuRepository.save(ns);

        // 3. Nếu là Giảng viên -> Lưu vào bảng GiangVien
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
            BoMon bm = boMonRepository.findById(form.getMaBoMon())
                    .orElseThrow(() -> new ResourceNotFoundException("bomon.notFound", form.getMaBoMon()));

            GiangVien gv = giangVienRepository.findById(maNS).orElseGet(() -> new GiangVien(ns, form.getHocHam(), form.getHocVi(), bm));
            gv.setHocHam(form.getHocHam());
            gv.setHocVi(form.getHocVi());
            gv.setBoMon(bm);
            giangVienRepository.save(gv);
        }
    }

    @Transactional
    public void delete(String maNS) {
        NhanSu ns = getOrThrow(maNS);

        if (lopHanhChinhRepository.existsByCoVan_MaNS(maNS)) {
            throw new BusinessRuleException("Nhân sự này đang được phân công làm Cố vấn học tập, không thể xóa!");
        }
        if (lopHocPhanRepository.existsByGiangVien_MaNS(maNS)) {
            throw new BusinessRuleException("Giảng viên này đang phụ trách lớp học phần, không thể xóa!");
        }

        giangVienRepository.deleteById(maNS);
        TaiKhoan tk = ns.getTaiKhoan();
        nhanSuRepository.delete(ns);
        if (tk != null) {
            taiKhoanRepository.delete(tk);
        }
    }
}
