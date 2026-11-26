package com.qldtkhoa.qldtkhoa_app.config;

import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final VaiTroRepository vaiTroRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanSuRepository nhanSuRepository;
    private final GiangVienRepository giangVienRepository;
    private final KhoaRepository khoaRepository;
    private final BoMonRepository boMonRepository;
    private final NganhHocRepository nganhHocRepository;
    private final KhoaNhapHocRepository khoaNhapHocRepository;
    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final SinhVienRepository sinhVienRepository;
    private final MonHocRepository monHocRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedSampleDataIfEmpty();
    }

    private void seedRoles() {
        ensureRole("ADMIN", "Quản trị viên hệ thống");
        ensureRole("PHONG_DAO_TAO", "Chuyên viên Phòng Đào tạo");
        ensureRole("GIANG_VIEN", "Giảng viên");
        ensureRole("SINH_VIEN", "Sinh viên");
    }

    private void ensureRole(String maVaiTro, String tenVaiTro) {
        if (!vaiTroRepository.existsById(maVaiTro)) {
            vaiTroRepository.save(new VaiTro(maVaiTro, tenVaiTro));
            log.info("Đã khởi tạo vai trò {}", maVaiTro);
        }
    }

    private void seedSampleDataIfEmpty() {
        if (taiKhoanRepository.count() > 0) {
            return;
        }

        log.info("Cơ sở dữ liệu đang trống tài khoản, tiến hành nạp dữ liệu mẫu ban đầu để kiểm thử...");

        VaiTro roleAdmin = vaiTroRepository.findById("ADMIN").orElseThrow();
        VaiTro rolePdt = vaiTroRepository.findById("PHONG_DAO_TAO").orElseThrow();
        VaiTro roleGv = vaiTroRepository.findById("GIANG_VIEN").orElseThrow();
        VaiTro roleSv = vaiTroRepository.findById("SINH_VIEN").orElseThrow();

        // 1. Khoa & Bộ môn
        Khoa khoaCntt = khoaRepository.findById("FIT").orElseGet(() ->
                khoaRepository.save(new Khoa("FIT", "Công nghệ thông tin", 2005)));
        BoMon bmKtp = boMonRepository.findById("BM_CNPM").orElseGet(() ->
                boMonRepository.save(new BoMon("BM_CNPM", "Công nghệ phần mềm", khoaCntt)));
        BoMon bmHtth = boMonRepository.findById("BM_HTTT").orElseGet(() ->
                boMonRepository.save(new BoMon("BM_HTTT", "Hệ thống thông tin", khoaCntt)));

        // 2. Ngành học & Khóa nhập học & Học kỳ
        NganhHoc nganhCntt = nganhHocRepository.findById("7480201").orElseGet(() ->
                nganhHocRepository.save(new NganhHoc("7480201", "Công nghệ thông tin", khoaCntt)));
        KhoaNhapHoc k2024 = khoaNhapHocRepository.findById("K2024").orElseGet(() ->
                khoaNhapHocRepository.save(new KhoaNhapHoc("K2024", 2024)));
        hocKyNamHocRepository.findById("HK1_2025").orElseGet(() ->
                hocKyNamHocRepository.save(new HocKyNamHoc("HK1_2025", "Học kỳ 1 năm học 2025-2026", 2025,
                        LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 15))));

        // 3. Môn học mẫu
        if (monHocRepository.count() == 0) {
            monHocRepository.save(new MonHoc("IT101", "Nhập môn Lập trình", 3, 2, 1, 0.2, 0.3, 0.5, bmKtp));
            monHocRepository.save(new MonHoc("IT102", "Cấu trúc dữ liệu và giải thuật", 3, 2, 1, 0.2, 0.3, 0.5, bmKtp));
            monHocRepository.save(new MonHoc("IT201", "Cơ sở dữ liệu", 3, 2, 1, 0.2, 0.3, 0.5, bmHtth));
        }

        // 4. Tài khoản & Nhân sự Admin
        TaiKhoan tkAdmin = taiKhoanRepository.save(new TaiKhoan("admin@qldt.edu.vn", passwordEncoder.encode("Admin@123"), roleAdmin, true));
        nhanSuRepository.save(new NhanSu("NS_ADMIN", "Quản Trị Viên", tkAdmin));

        // 5. Tài khoản & Nhân sự Phòng Đào Tạo
        TaiKhoan tkPdt = taiKhoanRepository.save(new TaiKhoan("pdt@qldt.edu.vn", passwordEncoder.encode("Pdt@123"), rolePdt, true));
        nhanSuRepository.save(new NhanSu("NS_PDT", "Trần Thị Giáo Vụ", tkPdt));

        // 6. Tài khoản, Nhân sự & Giảng viên
        TaiKhoan tkGv = taiKhoanRepository.save(new TaiKhoan("gv.nguyenan@qldt.edu.vn", passwordEncoder.encode("Gv@123"), roleGv, true));
        NhanSu nsGv = nhanSuRepository.save(new NhanSu("GV01", "TS. Nguyễn Văn An", tkGv));
        GiangVien gv = giangVienRepository.save(new GiangVien(nsGv, null, "Tiến sĩ", bmKtp));

        // 7. Lớp hành chính & Cố vấn
        LopHanhChinh lopHc = lopHanhChinhRepository.findById("CNTT-K24A").orElseGet(() ->
                lopHanhChinhRepository.save(new LopHanhChinh("CNTT-K24A", "Lớp Công nghệ thông tin K24A", nganhCntt, k2024, gv)));

        // 8. Tài khoản & Sinh viên
        TaiKhoan tkSv = taiKhoanRepository.save(new TaiKhoan("sv.lehoa@qldt.edu.vn", passwordEncoder.encode("Sv@123"), roleSv, true));
        sinhVienRepository.save(new SinhVien("SV2024001", "Lê Thị Hoa", LocalDate.of(2006, 5, 15), GioiTinh.NU, tkSv, TrangThaiSinhVien.DANG_HOC, lopHc));

        log.info("Đã nạp dữ liệu mẫu ban đầu: 4 tài khoản (admin, pdt, gv.nguyenan, sv.lehoa) kèm master data!");
    }
}
