package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.HocLucSummary;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GpaRepeatPolicy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HocLucServiceTest {

    @Mock
    private BangDiemSinhVienViewRepository bangDiemViewRepository;

    private HocLucService serviceWithPolicy(GpaRepeatPolicy repeatPolicy) {
        AcademicPolicy policy = new AcademicPolicy(
                4.0, 14, 25, 14, 28,
                repeatPolicy, 5, "0 5 0 * * *", "Asia/Ho_Chi_Minh"
        );
        return new HocLucService(bangDiemViewRepository, policy);
    }

    private BangDiemSinhVienView mockView(String maMon, String maHK, String tenHK, int namHoc,
                                          int soTinChi, Boolean tinhVaoGPA, TrangThaiDangKy regStatus,
                                          TrangThaiLop lopStatus, Double diemTong, Double diemHe4) {
        BangDiemSinhVienView v = mock(BangDiemSinhVienView.class);
        lenient().when(v.getMaMon()).thenReturn(maMon);
        lenient().when(v.getMaHK()).thenReturn(maHK);
        lenient().when(v.getTenHK()).thenReturn(tenHK);
        lenient().when(v.getNamHoc()).thenReturn(namHoc);
        lenient().when(v.getSoTinChi()).thenReturn(soTinChi);
        lenient().when(v.getTinhVaoGPA()).thenReturn(tinhVaoGPA);
        lenient().when(v.getTrangThaiDangKy()).thenReturn(regStatus);
        lenient().when(v.getTrangThaiLop()).thenReturn(lopStatus);
        lenient().when(v.getDiemTong()).thenReturn(diemTong);
        lenient().when(v.getDiemHe4()).thenReturn(diemHe4);
        return v;
    }

    @Test
    @DisplayName("evaluateSummary: Chính sách HIGHEST (GATE 3a) chọn lần điểm cao nhất và không cộng đúp tín chỉ")
    void evaluateSummary_retakeHighestPolicy_picksHighestScoreAndCountsCreditOnce() {
        HocLucService service = serviceWithPolicy(GpaRepeatPolicy.HIGHEST);

        BangDiemSinhVienView attempt1 = mockView("INT2204", "20231", "Học kỳ 1", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 8.6, 4.0);
        BangDiemSinhVienView attempt2 = mockView("INT2204", "20232", "Học kỳ 2", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 6.0, 2.0);

        HocLucSummary summary = service.evaluateSummary(List.of(attempt1, attempt2));

        assertThat(summary.tongTCTichLuy()).isEqualTo(3);
        assertThat(summary.cpa4()).isEqualTo(4.0);
        assertThat(summary.xepLoai()).isEqualTo("Xuất sắc");
    }

    @Test
    @DisplayName("evaluateSummary: Chính sách LATEST chọn lần học mới nhất dù điểm thấp hơn")
    void evaluateSummary_retakeLatestPolicy_picksLatestAttempt() {
        HocLucService service = serviceWithPolicy(GpaRepeatPolicy.LATEST);

        BangDiemSinhVienView attempt1 = mockView("INT2204", "20231", "Học kỳ 1", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 8.6, 4.0);
        BangDiemSinhVienView attempt2 = mockView("INT2204", "20232", "Học kỳ 2", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 6.0, 2.0);

        HocLucSummary summary = service.evaluateSummary(List.of(attempt1, attempt2));

        assertThat(summary.tongTCTichLuy()).isEqualTo(3);
        assertThat(summary.cpa4()).isEqualTo(2.0);
        assertThat(summary.xepLoai()).isEqualTo("Trung bình");
    }

    @Test
    @DisplayName("evaluateSummary: Môn TinhVaoGPA=false (GATE 3b) cộng tín chỉ tích lũy khi đạt nhưng loại khỏi GPA/CPA")
    void evaluateSummary_nonGpaCoursePassed_countsCreditButExcludedFromGpa() {
        HocLucService service = serviceWithPolicy(GpaRepeatPolicy.HIGHEST);

        BangDiemSinhVienView core = mockView("INT2204", "20231", "Học kỳ 1", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 8.5, 4.0);
        BangDiemSinhVienView pe = mockView("GDTC101", "20231", "Học kỳ 1", 2023,
                2, false, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 4.5, 1.0);

        HocLucSummary summary = service.evaluateSummary(List.of(core, pe));

        assertThat(summary.tongTCTichLuy()).isEqualTo(5);
        assertThat(summary.cpa4()).isEqualTo(4.0);
        assertThat(summary.termSummaries().get("Học kỳ 1 (2023)").gpa4()).isEqualTo(4.0);
        assertThat(summary.termSummaries().get("Học kỳ 1 (2023)").gradedCredits()).isEqualTo(3);
    }

    @Test
    @DisplayName("evaluateSummary: Cấm thi tính 0.0/F trong GPA và kích hoạt cảnh báo học vụ khi rớt >50% TC")
    void evaluateSummary_camThiAndLowGpa_triggersAcademicWarning() {
        HocLucService service = serviceWithPolicy(GpaRepeatPolicy.HIGHEST);

        BangDiemSinhVienView banned = mockView("INT2204", "20231", "Học kỳ 1", 2023,
                3, true, TrangThaiDangKy.CAM_THI, TrangThaiLop.DA_KHOA_DIEM, null, null);
        BangDiemSinhVienView failed = mockView("INT2205", "20231", "Học kỳ 1", 2023,
                3, true, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_KHOA_DIEM, 3.0, 0.0);

        HocLucSummary summary = service.evaluateSummary(List.of(banned, failed));

        assertThat(summary.tongTCTichLuy()).isEqualTo(0);
        assertThat(summary.cpa4()).isEqualTo(0.0);
        assertThat(summary.xepLoai()).isEqualTo("Kém");
        assertThat(summary.coCamThi()).isTrue();
        assertThat(summary.canhBaoHocVu()).isTrue();
        assertThat(summary.lyDoCanhBao()).hasSizeGreaterThanOrEqualTo(2);
    }
}
