package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.entity.NhatKyHeThong;
import com.qldtkhoa.qldtkhoa_app.repository.NhatKyHeThongRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private NhatKyHeThongRepository repository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-03-01T08:30:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @InjectMocks
    private AuditService service;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("log: Lấy actor từ SecurityContext và dùng Clock chuẩn")
    void log_usesAuthenticatedUserAndClock() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@truong.edu.vn", "N/A", List.of()));

        service.log("EDIT", "KetQua", "LHP01", "7.0", "8.0", "Phúc khảo");

        ArgumentCaptor<NhatKyHeThong> captor = ArgumentCaptor.forClass(NhatKyHeThong.class);
        verify(repository).save(captor.capture());
        NhatKyHeThong saved = captor.getValue();
        assertThat(saved.getNguoiThucHien()).isEqualTo("admin@truong.edu.vn");
        assertThat(saved.getThoiGian()).isEqualTo(LocalDateTime.now(clock));
        assertThat(saved.getLyDo()).isEqualTo("Phúc khảo");
    }

    @Test
    @DisplayName("logAs: Cắt chuỗi quá độ dài cột DB và mặc định SYSTEM khi actor trống")
    void logAs_truncatesLongFieldsAndDefaultsActorToSystem() {
        String longReason = "A".repeat(300);
        service.logAs("   ", "X".repeat(80), "Y".repeat(80), "Z".repeat(150), null, null, longReason);

        ArgumentCaptor<NhatKyHeThong> captor = ArgumentCaptor.forClass(NhatKyHeThong.class);
        verify(repository).save(captor.capture());
        NhatKyHeThong saved = captor.getValue();
        assertThat(saved.getNguoiThucHien()).isEqualTo("SYSTEM");
        assertThat(saved.getHanhDong()).hasSize(50);
        assertThat(saved.getDoiTuong()).hasSize(50);
        assertThat(saved.getMaBanGhi()).hasSize(100);
        assertThat(saved.getLyDo()).hasSize(255);
    }

    @Test
    @DisplayName("search: Thoát wildcard LIKE và đổi bộ lọc rỗng sang null")
    void search_escapesWildcardsAndNormalizesBlankFilters() {
        when(repository.search(any(), any(), any(), any())).thenReturn(Page.empty());

        service.search("100%_test", "   ", "", PageRequest.of(0, 20));

        verify(repository).search(eq("100\\%\\_test"), isNull(), isNull(), any());
    }

    @Test
    @DisplayName("findForExport: Thoát wildcard, chuẩn hóa bộ lọc rỗng và giới hạn 5000 dòng không kèm Sort.by")
    void findForExport_escapesWildcardsAndQueriesRepository() {
        when(repository.findForExport(any(), any(), any(), any())).thenReturn(List.of());

        service.findForExport("100%_", "  ", null);

        verify(repository).findForExport(eq("100\\%\\_"), isNull(), isNull(), eq(PageRequest.of(0, 5000)));
    }
}
