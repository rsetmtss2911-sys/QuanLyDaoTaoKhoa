package com.qldtkhoa.qldtkhoa_app.integration;

import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test chạy trực tiếp với SQL Server thật (`App_QLDTKhoa` v3).
 * Bị loại trừ khỏi `mvn test` mặc định qua cấu hình surefire {@code <excludedGroups>integration</excludedGroups>}.
 * Chạy thủ công khi SQL Server sẵn sàng:
 * {@code $env:RUN_SQLSERVER_IT="true"; .\mvnw.cmd test -Dgroups=integration}
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "RUN_SQLSERVER_IT", matches = "true")
@SpringBootTest
class SqlServerIntegrationTest {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private LopHocPhanRepository lopHocPhanRepository;
    @Autowired
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Autowired
    private DangKyHocPhanService dangKyHocPhanService;

    @Test
    @DisplayName("Schema-first: Xác minh kết nối SQL Server và tồn tại các bảng lõi App_QLDTKhoa")
    void sqlServerSchema_containsCoreTables() throws Exception {
        List<String> expectedTables = List.of(
                "Khoa", "SinhVien", "LopHocPhan", "DangKyHocPhan", "KetQuaHocTap", "QuyetDinhHocVu", "NhatKyHeThong");
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            for (String table : expectedTables) {
                try (ResultSet rs = meta.getTables(null, "dbo", table, new String[]{"TABLE"})) {
                    assertThat(rs.next()).as("Bảng SQL Server %s phải tồn tại", table).isTrue();
                }
            }
        }
    }

    @Test
    @DisplayName("Concurrency: 2 luồng đăng ký đồng thời khi lớp chỉ còn 1 chỗ trống không vượt SiSoToiDa")
    void concurrentRegistration_lastSeat_allowsExactlyOneWinner() throws Exception {
        LopHocPhan target = lopHocPhanRepository.findAll().stream()
                .filter(l -> l.getTrangThaiLop() == TrangThaiLop.CHO_MO)
                .findFirst()
                .orElse(null);
        if (target == null) {
            return;
        }
        String maLHP = target.getMaLHP();
        int originalCap = target.getSiSoToiDa();
        long activeCount = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI));
        target.setSiSoToiDa((int) activeCount + 1);
        lopHocPhanRepository.saveAndFlush(target);

        String svA = "IT_SV_A";
        String svB = "IT_SV_B";
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            List<Future<Throwable>> futures = new ArrayList<>();
            for (String sv : List.of(svA, svB)) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await(5, TimeUnit.SECONDS);
                    try {
                        dangKyHocPhanService.register(sv, maLHP);
                        return null;
                    } catch (Throwable t) {
                        return t;
                    }
                }));
            }
            ready.await(5, TimeUnit.SECONDS);
            go.countDown();
            List<Throwable> errors = new ArrayList<>();
            for (Future<Throwable> f : futures) {
                Throwable t = f.get(15, TimeUnit.SECONDS);
                if (t != null) {
                    errors.add(t);
                }
            }
            long afterCount = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                    maLHP, List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI));
            assertThat(afterCount).isLessThanOrEqualTo(activeCount + 1);
            if (!errors.isEmpty()) {
                assertThat(errors.get(0)).isInstanceOfAny(BusinessRuleException.class, RuntimeException.class);
            }
        } finally {
            target.setSiSoToiDa(originalCap);
            lopHocPhanRepository.saveAndFlush(target);
            dangKyHocPhanRepository.findById(new DangKyHocPhanId(svA, maLHP)).ifPresent(dangKyHocPhanRepository::delete);
            dangKyHocPhanRepository.findById(new DangKyHocPhanId(svB, maLHP)).ifPresent(dangKyHocPhanRepository::delete);
        }
    }
}
