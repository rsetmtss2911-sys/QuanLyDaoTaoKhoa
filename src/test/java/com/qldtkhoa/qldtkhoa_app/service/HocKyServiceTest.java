package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.HocKyForm;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.HocKyNamHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HocKyServiceTest {

    @Mock
    private HocKyNamHocRepository hocKyNamHocRepository;

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2025-10-15T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @InjectMocks
    private HocKyService hocKyService;

    @Test
    @DisplayName("getCurrentTerm: Ưu tiên học kỳ đang diễn ra tại Clock.now(), fallback sang kỳ đã bắt đầu gần nhất")
    void getCurrentTerm_coveringOrFallback_returnsExpectedTerm() {
        HocKyNamHoc current = mock(HocKyNamHoc.class);
        when(hocKyNamHocRepository.findCoveringTerms(LocalDate.of(2025, 10, 15)))
                .thenReturn(List.of(current));

        assertThat(hocKyService.getCurrentTerm()).isSameAs(current);

        HocKyNamHoc latestStarted = mock(HocKyNamHoc.class);
        when(hocKyNamHocRepository.findCoveringTerms(LocalDate.of(2025, 10, 15)))
                .thenReturn(List.of());
        when(hocKyNamHocRepository.findStartedBeforeOrOn(LocalDate.of(2025, 10, 15)))
                .thenReturn(List.of(latestStarted));

        assertThat(hocKyService.getCurrentTerm()).isSameAs(latestStarted);

        HocKyNamHoc newest = mock(HocKyNamHoc.class);
        when(hocKyNamHocRepository.findStartedBeforeOrOn(LocalDate.of(2025, 10, 15)))
                .thenReturn(List.of());
        when(hocKyNamHocRepository.findTopByOrderByNamHocDescMaHKDesc())
                .thenReturn(Optional.of(newest));

        assertThat(hocKyService.getCurrentTerm()).isSameAs(newest);
    }

    @Test
    @DisplayName("create: Chặn tạo học kỳ có khoảng thời gian chồng lấn với học kỳ khác")
    void create_overlappingDates_throwsBusinessRuleException() {
        HocKyForm form = validForm();
        when(hocKyNamHocRepository.existsById("20251")).thenReturn(false);
        when(hocKyNamHocRepository.existsOverlappingTerm(null, form.getNgayBatDau(), form.getNgayKetThuc()))
                .thenReturn(true);

        assertThatThrownBy(() -> hocKyService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("hocky.dateOverlap");
    }

    @Test
    @DisplayName("create: Kiểm tra thứ tự các mốc đăng ký / rút / khóa điểm khớp CHK_HocKy_MocDangKy")
    void create_invalidMilestoneOrder_throwsBusinessRuleException() {
        HocKyForm form = validForm();
        form.setNgayMoDangKy(LocalDate.of(2025, 8, 20));
        form.setNgayDongDangKy(LocalDate.of(2025, 8, 10)); // Đóng trước mở

        when(hocKyNamHocRepository.existsById("20251")).thenReturn(false);
        when(hocKyNamHocRepository.existsOverlappingTerm(null, form.getNgayBatDau(), form.getNgayKetThuc()))
                .thenReturn(false);

        assertThatThrownBy(() -> hocKyService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("hocky.milestoneOrder");
    }

    private HocKyForm validForm() {
        HocKyForm form = new HocKyForm();
        form.setMaHK("20251");
        form.setTenHK("Học kỳ 1");
        form.setNamHoc(2025);
        form.setNgayBatDau(LocalDate.of(2025, 9, 1));
        form.setNgayKetThuc(LocalDate.of(2025, 12, 31));
        form.setNgayMoDangKy(LocalDate.of(2025, 8, 15));
        form.setNgayDongDangKy(LocalDate.of(2025, 9, 14));
        form.setHanRutHocPhan(LocalDate.of(2025, 9, 28));
        form.setHanKhoaDiem(LocalDate.of(2026, 1, 10));
        return form;
    }
}
