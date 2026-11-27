package com.qldtkhoa.qldtkhoa_app.repository;

import com.qldtkhoa.qldtkhoa_app.dto.KhoaRow;
import com.qldtkhoa.qldtkhoa_app.dto.NhanSuRow;
import com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.BoMon;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.entity.Khoa;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.NhatKyHeThong;
import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryDataJpaTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private KhoaRepository khoaRepository;
    @Autowired
    private BoMonRepository boMonRepository;
    @Autowired
    private NganhHocRepository nganhHocRepository;
    @Autowired
    private KhoaNhapHocRepository khoaNhapHocRepository;
    @Autowired
    private VaiTroRepository vaiTroRepository;
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Autowired
    private NhanSuRepository nhanSuRepository;
    @Autowired
    private GiangVienRepository giangVienRepository;
    @Autowired
    private LopHanhChinhRepository lopHanhChinhRepository;
    @Autowired
    private SinhVienRepository sinhVienRepository;
    @Autowired
    private HocKyNamHocRepository hocKyNamHocRepository;
    @Autowired
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;
    @Autowired
    private NhatKyHeThongRepository nhatKyHeThongRepository;

    @Test
    @DisplayName("Converter & QĐHV đến hạn: Lưu nhãn tiếng Việt đúng chuẩn và truy vấn đúng sinh viên có QĐHV đến hạn")
    void convertersAndDueDecisions_persistAndQueryCorrectly() {
        VaiTro vtGv = vaiTroRepository.saveAndFlush(new VaiTro("GIANG_VIEN", "Giảng viên"));
        VaiTro vtSv = vaiTroRepository.saveAndFlush(new VaiTro("SINH_VIEN", "Sinh viên"));
        Khoa khoa = khoaRepository.saveAndFlush(new Khoa("CNTT", "Công nghệ thông tin", 1995));
        BoMon bm = boMonRepository.saveAndFlush(new BoMon("CNPM", "Công nghệ phần mềm", khoa));
        NganhHoc nganh = nganhHocRepository.saveAndFlush(new NganhHoc("CNTT_N", "CNTT", khoa));
        KhoaNhapHoc kh = khoaNhapHocRepository.saveAndFlush(new KhoaNhapHoc("K68", 2023));

        TaiKhoan tkGv = taiKhoanRepository.saveAndFlush(new TaiKhoan("gv1@edu.vn", "$2a$10$hash", vtGv, true));
        NhanSu ns = nhanSuRepository.saveAndFlush(new NhanSu("GV01", "TS. Nguyễn Văn A", tkGv));
        GiangVien gv = giangVienRepository.saveAndFlush(new GiangVien(ns, "PGS", "Tiến sĩ", bm));
        LopHanhChinh lhc = lopHanhChinhRepository.saveAndFlush(new LopHanhChinh("K68_A", "K68 A", nganh, kh, gv));

        TaiKhoan tkSv1 = taiKhoanRepository.saveAndFlush(new TaiKhoan("sv1@edu.vn", "$2a$10$hash", vtSv, true));
        SinhVien sv1 = sinhVienRepository.saveAndFlush(new SinhVien(
                "SV01", "Trần Thị Mai", LocalDate.of(2005, 8, 15), GioiTinh.NU, tkSv1, TrangThaiSinhVien.BAO_LUU, lhc));

        TaiKhoan tkSv2 = taiKhoanRepository.saveAndFlush(new TaiKhoan("sv2@edu.vn", "$2a$10$hash", vtSv, true));
        SinhVien sv2 = sinhVienRepository.saveAndFlush(new SinhVien(
                "SV02", "Lê Văn B", LocalDate.of(2005, 3, 20), GioiTinh.NAM, tkSv2, TrangThaiSinhVien.DANG_HOC, lhc));

        quyetDinhHocVuRepository.saveAndFlush(new QuyetDinhHocVu(
                "QD_DUE", sv1, LoaiQuyetDinh.BAO_LUU, LocalDate.of(2026, 2, 1), "Bảo lưu sức khỏe"));
        quyetDinhHocVuRepository.saveAndFlush(new QuyetDinhHocVu(
                "QD_FUT", sv2, LoaiQuyetDinh.DINH_CHI, LocalDate.of(2026, 5, 1), "Hẹn tương lai"));

        em.flush();
        em.clear();

        Object rawGender = em.createNativeQuery("SELECT GioiTinh FROM SinhVien WHERE MaSV = 'SV01'").getSingleResult();
        Object rawStatus = em.createNativeQuery("SELECT TrangThaiHienTai FROM SinhVien WHERE MaSV = 'SV01'").getSingleResult();
        assertThat(rawGender).isEqualTo("Nữ");
        assertThat(rawStatus).isEqualTo("Bảo lưu");

        SinhVien loaded = sinhVienRepository.findById("SV01").orElseThrow();
        assertThat(loaded.getGioiTinh()).isEqualTo(GioiTinh.NU);
        assertThat(loaded.getTrangThaiHienTai()).isEqualTo(TrangThaiSinhVien.BAO_LUU);

        List<String> dueIds = quyetDinhHocVuRepository.findStudentIdsWithDueDecisions(LocalDate.of(2026, 2, 1));
        assertThat(dueIds).containsExactly("SV01");
    }

    @Test
    @DisplayName("JPQL Projection & LikeUtils.escape: KhoaRepository.searchRows và NhanSuRepository.searchRows")
    void projectionsAndLikeEscape_returnExpectedRowsSafely() {
        khoaRepository.saveAndFlush(new Khoa("K_100", "Khoa 100%_DacBiet", 2000));
        khoaRepository.saveAndFlush(new Khoa("K_999", "Khoa 1000_Thuong", 2001));
        Khoa khoa = khoaRepository.findById("K_100").orElseThrow();
        BoMon bm = boMonRepository.saveAndFlush(new BoMon("BM1", "Hệ thống thông tin", khoa));

        VaiTro vtGv = vaiTroRepository.saveAndFlush(new VaiTro("GIANG_VIEN", "Giảng viên"));
        VaiTro vtAd = vaiTroRepository.saveAndFlush(new VaiTro("ADMIN", "Quản trị"));
        TaiKhoan tkGv = taiKhoanRepository.saveAndFlush(new TaiKhoan("gv@edu.vn", "$2a$10$hash", vtGv, true));
        NhanSu nsGv = nhanSuRepository.saveAndFlush(new NhanSu("GV1", "TS. Trần A", tkGv));
        giangVienRepository.saveAndFlush(new GiangVien(nsGv, null, "Tiến sĩ", bm));

        TaiKhoan tkAd = taiKhoanRepository.saveAndFlush(new TaiKhoan("ad@edu.vn", "$2a$10$hash", vtAd, true));
        nhanSuRepository.saveAndFlush(new NhanSu("AD1", "Quản trị 1", tkAd));

        em.flush();
        em.clear();

        Page<KhoaRow> escapedSearch = khoaRepository.searchRows(
                LikeUtils.escape("100%_"), PageRequest.of(0, 10, Sort.by("maKhoa").ascending()));
        assertThat(escapedSearch.getContent()).extracting(r -> r.maKhoa()).containsExactly("K_100");
        assertThat(escapedSearch.getContent().get(0).soBoMon()).isEqualTo(1L);

        Page<NhanSuRow> nsPage = nhanSuRepository.searchRows("", "GIANG_VIEN", PageRequest.of(0, 10, Sort.by("maNS").ascending()));
        assertThat(nsPage.getContent()).hasSize(1);
        assertThat(nsPage.getContent().get(0).isGiangVien()).isTrue();
        assertThat(nsPage.getContent().get(0).tenBoMon()).isEqualTo("Hệ thống thông tin");
    }

    @Test
    @DisplayName("HocKyNamHocRepository.existsOverlappingTerm: Phát hiện học kỳ trùng khoảng thời gian và loại trừ chính nó")
    void existsOverlappingTerm_detectsOverlapAndExcludesSelf() {
        hocKyNamHocRepository.saveAndFlush(new HocKyNamHoc(
                "HK1_2025", "Học kỳ 1", 2025, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 15)));
        em.flush();

        assertThat(hocKyNamHocRepository.existsOverlappingTerm(null, LocalDate.of(2026, 1, 10), LocalDate.of(2026, 5, 1))).isTrue();
        assertThat(hocKyNamHocRepository.existsOverlappingTerm("HK1_2025", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 15))).isFalse();
        assertThat(hocKyNamHocRepository.existsOverlappingTerm(null, LocalDate.of(2026, 1, 16), LocalDate.of(2026, 5, 30))).isFalse();
    }

    @Test
    @DisplayName("findForExport: SinhVienRepository và NhatKyHeThongRepository sắp xếp ổn định qua JPQL ORDER BY")
    void findForExport_returnsOrderedResultsWithEscapedFilter() {
        VaiTro vtGv = vaiTroRepository.saveAndFlush(new VaiTro("GIANG_VIEN", "Giảng viên"));
        VaiTro vtSv = vaiTroRepository.saveAndFlush(new VaiTro("SINH_VIEN", "Sinh viên"));
        Khoa khoa = khoaRepository.saveAndFlush(new Khoa("CNTT", "CNTT", 1995));
        BoMon bm = boMonRepository.saveAndFlush(new BoMon("CNPM", "CNPM", khoa));
        NganhHoc nganh = nganhHocRepository.saveAndFlush(new NganhHoc("CNTT_N", "CNTT", khoa));
        KhoaNhapHoc kh = khoaNhapHocRepository.saveAndFlush(new KhoaNhapHoc("K68", 2023));
        TaiKhoan tkGv = taiKhoanRepository.saveAndFlush(new TaiKhoan("gv@edu.vn", "$2a$10$hash", vtGv, true));
        NhanSu ns = nhanSuRepository.saveAndFlush(new NhanSu("GV01", "TS. A", tkGv));
        GiangVien gv = giangVienRepository.saveAndFlush(new GiangVien(ns, null, "TS", bm));
        LopHanhChinh lhc = lopHanhChinhRepository.saveAndFlush(new LopHanhChinh("K68_A", "K68 A", nganh, kh, gv));

        TaiKhoan tkB = taiKhoanRepository.saveAndFlush(new TaiKhoan("sv2@edu.vn", "$2a$10$hash", vtSv, true));
        TaiKhoan tkA = taiKhoanRepository.saveAndFlush(new TaiKhoan("sv1@edu.vn", "$2a$10$hash", vtSv, true));
        sinhVienRepository.saveAndFlush(new SinhVien(
                "SV02", "Trần B", LocalDate.of(2005, 1, 1), GioiTinh.NAM, tkB, TrangThaiSinhVien.DANG_HOC, lhc));
        sinhVienRepository.saveAndFlush(new SinhVien(
                "SV01", "Nguyễn A", LocalDate.of(2005, 1, 1), GioiTinh.NU, tkA, TrangThaiSinhVien.DANG_HOC, lhc));

        nhatKyHeThongRepository.saveAndFlush(new NhatKyHeThong(
                LocalDateTime.of(2026, 3, 1, 9, 0), "admin@edu.vn", "EDIT", "KetQua", "L1", "7", "8", "100%_ok"));
        nhatKyHeThongRepository.saveAndFlush(new NhatKyHeThong(
                LocalDateTime.of(2026, 3, 2, 9, 0), "admin@edu.vn", "EDIT", "KetQua", "L2", "6", "9", "100%_new"));
        nhatKyHeThongRepository.saveAndFlush(new NhatKyHeThong(
                LocalDateTime.of(2026, 3, 3, 9, 0), "admin@edu.vn", "EDIT", "KetQua", "L3", "5", "8", "1000_skip"));

        em.flush();
        em.clear();

        List<SinhVien> svExport = sinhVienRepository.findForExport("", null, null, PageRequest.of(0, 5000));
        assertThat(svExport).extracting(sv -> sv.getMaSV()).containsExactly("SV01", "SV02");

        List<NhatKyRow> logExport = nhatKyHeThongRepository.findForExport(
                LikeUtils.escape("100%_"), null, null, PageRequest.of(0, 5000));
        assertThat(logExport).extracting(r -> r.maBanGhi()).containsExactly("L2", "L1");
    }
}
