package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.entity.ChuongTrinhDaoTao;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChuongTrinhDaoTaoServiceTest {

    @Mock
    private ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;

    @Mock
    private NganhHocRepository nganhHocRepository;

    @Mock
    private KhoaNhapHocRepository khoaNhapHocRepository;

    @Mock
    private MonHocRepository monHocRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private BangDiemSinhVienViewRepository bangDiemViewRepository;

    @Mock
    private AcademicPolicy academicPolicy;

    @InjectMocks
    private ChuongTrinhDaoTaoService chuongTrinhDaoTaoService;

    @Test
    @DisplayName("getTienDoCTDT: Tính tiến độ CTĐT chính xác theo ngưỡng passScore từ AcademicPolicy")
    void getTienDoCTDT_calculatesProgressCorrectly() {
        when(academicPolicy.passScore()).thenReturn(4.0);

        SinhVien sv = mock(SinhVien.class);
        LopHanhChinh lhc = mock(LopHanhChinh.class);
        NganhHoc nganh = mock(NganhHoc.class);
        KhoaNhapHoc khoa = mock(KhoaNhapHoc.class);

        when(sv.getMaSV()).thenReturn("SV001");
        when(sv.getHoTen()).thenReturn("Nguyễn Văn A");
        when(sv.getLopHanhChinh()).thenReturn(lhc);
        when(lhc.getNganhHoc()).thenReturn(nganh);
        when(lhc.getKhoaNhapHoc()).thenReturn(khoa);
        when(nganh.getMaNganh()).thenReturn("CNTT");
        when(nganh.getTenNganh()).thenReturn("Công nghệ Thông tin");
        when(khoa.getMaKhoaHoc()).thenReturn("K24");

        when(sinhVienRepository.findById("SV001")).thenReturn(Optional.of(sv));

        MonHoc mh1 = mock(MonHoc.class);
        when(mh1.getMaMon()).thenReturn("INT1001");
        when(mh1.getTenMon()).thenReturn("Nhập môn lập trình");
        when(mh1.getSoTinChi()).thenReturn(3);

        MonHoc mh2 = mock(MonHoc.class);
        when(mh2.getMaMon()).thenReturn("INT1002");
        when(mh2.getTenMon()).thenReturn("Cấu trúc dữ liệu");
        when(mh2.getSoTinChi()).thenReturn(3);

        ChuongTrinhDaoTao ctdt1 = mock(ChuongTrinhDaoTao.class);
        when(ctdt1.getNganhHoc()).thenReturn(nganh);
        when(ctdt1.getKhoaNhapHoc()).thenReturn(khoa);
        when(ctdt1.getMonHoc()).thenReturn(mh1);
        when(ctdt1.getHocKyChuan()).thenReturn(1);

        ChuongTrinhDaoTao ctdt2 = mock(ChuongTrinhDaoTao.class);
        when(ctdt2.getNganhHoc()).thenReturn(nganh);
        when(ctdt2.getKhoaNhapHoc()).thenReturn(khoa);
        when(ctdt2.getMonHoc()).thenReturn(mh2);
        when(ctdt2.getHocKyChuan()).thenReturn(2);

        when(chuongTrinhDaoTaoRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc("CNTT", "K24"))
                .thenReturn(List.of(ctdt1, ctdt2));

        BangDiemSinhVienView g1 = mock(BangDiemSinhVienView.class);
        when(g1.getMaMon()).thenReturn("INT1001");
        when(g1.getDiemTong()).thenReturn(7.5); // Pass (>= 4.0)

        BangDiemSinhVienView g2 = mock(BangDiemSinhVienView.class);
        when(g2.getDiemTong()).thenReturn(3.5); // Fail (< 4.0)

        when(bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc("SV001"))
                .thenReturn(List.of(g1, g2));

        TienDoHocTapView view = chuongTrinhDaoTaoService.getTienDoCTDT("SV001");

        assertThat(view.maSV()).isEqualTo("SV001");
        assertThat(view.totalCurriculumCredits()).isEqualTo(6);
        assertThat(view.completedCredits()).isEqualTo(3);
        assertThat(view.progressPercent()).isEqualTo(50);
        assertThat(view.passedSubjects()).containsExactly("INT1001");
    }
}
