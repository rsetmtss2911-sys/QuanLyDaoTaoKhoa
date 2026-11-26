package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDashboardView;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private GiangVienRepository giangVienRepository;

    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @Mock
    private LopHocPhanService lopHocPhanService;

    @Mock
    private MonHocRepository monHocRepository;

    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;

    @Mock
    private LopHanhChinhService lopHanhChinhService;

    @Mock
    private HocKyService hocKyService;

    @Mock
    private DangKyHocPhanService dangKyHocPhanService;

    @Mock
    private LichGiangDayRepository lichGiangDayRepository;

    @Mock
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;

    @Mock
    private ThongKeLopHocPhanViewRepository thongKeLopHocPhanViewRepository;

    @Mock
    private BangDiemSinhVienViewRepository bangDiemViewRepository;

    @Mock
    private AcademicPolicy academicPolicy;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        HocLucService hocLucService = new HocLucService(bangDiemViewRepository, academicPolicy);
        dashboardService = new DashboardService(
                sinhVienRepository, giangVienRepository, lopHocPhanRepository, lopHocPhanService,
                monHocRepository, lopHanhChinhRepository, lopHanhChinhService, quyetDinhHocVuRepository,
                thongKeLopHocPhanViewRepository, hocKyService, dangKyHocPhanService,
                lichGiangDayRepository, bangDiemViewRepository, academicPolicy, hocLucService
        );
    }

    @Test
    @DisplayName("getSinhVienDashboard: Sử dụng passScore từ AcademicPolicy để tính tín chỉ tích lũy và GPA")
    void getSinhVienDashboard_usesPassScoreFromPolicy() {
        when(academicPolicy.passScore()).thenReturn(4.0);
        when(academicPolicy.minCreditsPerTerm()).thenReturn(14);
        when(academicPolicy.maxCreditsPerTerm()).thenReturn(25);

        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("SV001");
        when(sv.getHoTen()).thenReturn("Nguyễn Văn A");
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);

        when(sinhVienRepository.findByIdWithDetails("SV001")).thenReturn(Optional.of(sv));

        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hk.getMaHK()).thenReturn("20241");
        when(hk.getTenHK()).thenReturn("Học kỳ 1");
        when(hk.getNamHoc()).thenReturn(2024);

        when(hocKyService.findAll()).thenReturn(List.of(hk));
        when(hocKyService.getOrThrow("20241")).thenReturn(hk);

        when(dangKyHocPhanService.getSelectedCourses("SV001", "20241")).thenReturn(Collections.emptyList());
        when(lichGiangDayRepository.findStudentWeeklyTimetable("SV001", "20241")).thenReturn(Collections.emptyList());
        when(quyetDinhHocVuRepository.findBySinhVien_MaSVOrderByNgayHieuLucDesc("SV001")).thenReturn(Collections.emptyList());

        BangDiemSinhVienView v1 = mock(BangDiemSinhVienView.class);
        when(v1.getMaMon()).thenReturn("INT1001");
        when(v1.getTenHK()).thenReturn("Học kỳ 1");
        when(v1.getNamHoc()).thenReturn(2024);
        when(v1.getDiemTong()).thenReturn(8.0);
        when(v1.getDiemHe4()).thenReturn(3.5);
        when(v1.getSoTinChi()).thenReturn(3);
        when(v1.getTinhVaoGPA()).thenReturn(true);

        BangDiemSinhVienView v2 = mock(BangDiemSinhVienView.class);
        when(v2.getMaMon()).thenReturn("INT1002");
        when(v2.getTenHK()).thenReturn("Học kỳ 1");
        when(v2.getNamHoc()).thenReturn(2024);
        when(v2.getDiemTong()).thenReturn(3.0); // Fail (< 4.0)
        when(v2.getTinhVaoGPA()).thenReturn(true);

        when(bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc("SV001"))
                .thenReturn(List.of(v1, v2));

        SinhVienDashboardView view = dashboardService.getSinhVienDashboard("SV001", "20241");

        assertThat(view.maSV()).isEqualTo("SV001");
        assertThat(view.totalCumulativeCredits()).isEqualTo(3);
        assertThat(view.cumulativeGpa4()).isEqualTo(3.5);
        assertThat(view.academicStanding()).isEqualTo("Giỏi");
        assertThat(view.minCreditsTerm()).isEqualTo(14);
        assertThat(view.maxCreditsTerm()).isEqualTo(25);

        verify(academicPolicy, atLeastOnce()).passScore();
    }
}
