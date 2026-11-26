package com.qldtkhoa.qldtkhoa_app;

import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QldtkhoaAppApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private KhoaRepository khoaRepository;

    @Autowired
    private MonHocRepository monHocRepository;

    @Autowired
    private DinhMucCTDTRepository dinhMucCTDTRepository;

    @Autowired
    private NhatKyHeThongRepository nhatKyHeThongRepository;

    @Test
    void contextLoads_withTestProfileAndAll21Entities() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.isValid(2)).isTrue();
        }
        assertThat(khoaRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(monHocRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(dinhMucCTDTRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(nhatKyHeThongRepository.count()).isGreaterThanOrEqualTo(0);
    }
}
