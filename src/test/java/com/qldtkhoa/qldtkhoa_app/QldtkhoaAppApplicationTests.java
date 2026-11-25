package com.qldtkhoa.qldtkhoa_app;

import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QldtkhoaAppApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(QldtkhoaAppApplicationTests.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private KhoaRepository khoaRepository;

    @Autowired
    private SinhVienRepository sinhVienRepository;

    @Autowired
    private MonHocRepository monHocRepository;

    @Autowired
    private LopHocPhanRepository lopHocPhanRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Autowired
    private KetQuaHocTapRepository ketQuaHocTapRepository;

    @Test
    void testDatabaseConnectionAndDataCounts() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.isValid(2)).isTrue();
            DatabaseMetaData metaData = conn.getMetaData();
            log.info("=== KẾT NỐI DATABASE THÀNH CÔNG ===");
            log.info("Database Product: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            log.info("Catalog (DB Name): {}", conn.getCatalog());
            log.info("Database URL: {}", metaData.getURL());
            log.info("Username: {}", metaData.getUserName());
        }

        long countKhoa = khoaRepository.count();
        long countMonHoc = monHocRepository.count();
        long countLopHocPhan = lopHocPhanRepository.count();
        long countSinhVien = sinhVienRepository.count();
        long countTaiKhoan = taiKhoanRepository.count();
        long countDangKy = dangKyHocPhanRepository.count();
        long countKetQua = ketQuaHocTapRepository.count();

        log.info("=== SỐ LIỆU BẢNG TRONG DATABASE ===");
        log.info("Khoa: {} bản ghi", countKhoa);
        log.info("Môn học: {} bản ghi", countMonHoc);
        log.info("Lớp học phần: {} bản ghi", countLopHocPhan);
        log.info("Sinh viên: {} bản ghi", countSinhVien);
        log.info("Tài khoản: {} bản ghi", countTaiKhoan);
        log.info("Đăng ký học phần: {} bản ghi", countDangKy);
        log.info("Kết quả học tập: {} bản ghi", countKetQua);

        assertThat(countKhoa).isGreaterThanOrEqualTo(0);
    }
}

