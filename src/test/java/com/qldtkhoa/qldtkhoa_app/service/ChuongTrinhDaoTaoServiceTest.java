package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.entity.ChuongTrinhDaoTao;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.DinhMucCTDT;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DinhMucCTDTRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChuongTrinhDaoTaoServiceTest {

    @Mock
    private ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;

    @Mock
    private DinhMucCTDTRepository dinhMucCTDTRepository;

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
    private DangKyHocPhanRepository dangKyHocPhanRepository;

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
        when(ctdt1.getKhoiKienThuc()).thenReturn(KhoiKienThuc.DAI_CUONG);
        when(ctdt1.getLoaiHocPhan()).thenReturn(LoaiHocPhan.BAT_BUOC);
        when(ctdt1.getHocKyChuan()).thenReturn(1);

        ChuongTrinhDaoTao ctdt2 = mock(ChuongTrinhDaoTao.class);
        when(ctdt2.getNganhHoc()).thenReturn(nganh);
        when(ctdt2.getKhoaNhapHoc()).thenReturn(khoa);
        when(ctdt2.getMonHoc()).thenReturn(mh2);
        when(ctdt2.getKhoiKienThuc()).thenReturn(KhoiKienThuc.CO_SO_NGANH);
        when(ctdt2.getLoaiHocPhan()).thenReturn(LoaiHocPhan.BAT_BUOC);
        when(ctdt2.getHocKyChuan()).thenReturn(2);

        when(chuongTrinhDaoTaoRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc("CNTT", "K24"))
                .thenReturn(List.of(ctdt1, ctdt2));
        when(dinhMucCTDTRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc("CNTT", "K24"))
                .thenReturn(List.of());
        when(dangKyHocPhanRepository.findBySinhVien_MaSV("SV001"))
                .thenReturn(List.of());

        BangDiemSinhVienView g1 = mock(BangDiemSinhVienView.class);
        when(g1.getMaMon()).thenReturn("INT1001");
        when(g1.getDiemTong()).thenReturn(7.5); // Pass (>= 4.0)

        BangDiemSinhVienView g2 = mock(BangDiemSinhVienView.class);
        when(g2.getMaMon()).thenReturn("INT1002");
        when(g2.getDiemTong()).thenReturn(3.5); // Fail (< 4.0)

        when(bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc("SV001"))
                .thenReturn(List.of(g1, g2));

        TienDoHocTapView view = chuongTrinhDaoTaoService.getTienDoCTDT("SV001");

        assertThat(view.maSV()).isEqualTo("SV001");
        assertThat(view.totalCurriculumCredits()).isEqualTo(6);
        assertThat(view.completedCredits()).isEqualTo(3);
        assertThat(view.progressPercent()).isEqualTo(50);
        assertThat(view.passedSubjects()).containsExactly("INT1001");
        assertThat(view.courseStatuses()).containsEntry("INT1001", "DA_TICH_LUY");
        assertThat(view.courseStatuses()).containsEntry("INT1002", "CHUA_DAT");
    }

    @Test
    @DisplayName("getTienDoCTDT: Áp dụng định mức tự chọn DinhMucCTDT và phân biệt đủ 4 trạng thái môn học")
    void getTienDoCTDT_withQuotasAndEnrolledCourse_computesBlockProgressAnd4Statuses() {
        when(academicPolicy.passScore()).thenReturn(4.0);

        SinhVien sv = mock(SinhVien.class);
        LopHanhChinh lhc = mock(LopHanhChinh.class);
        NganhHoc nganh = mock(NganhHoc.class);
        KhoaNhapHoc khoa = mock(KhoaNhapHoc.class);
        when(sv.getMaSV()).thenReturn("SV002");
        when(sv.getHoTen()).thenReturn("Trần Văn B");
        when(sv.getLopHanhChinh()).thenReturn(lhc);
        when(lhc.getNganhHoc()).thenReturn(nganh);
        when(lhc.getKhoaNhapHoc()).thenReturn(khoa);
        when(nganh.getMaNganh()).thenReturn("CNTT");
        when(nganh.getTenNganh()).thenReturn("CNTT");
        when(khoa.getMaKhoaHoc()).thenReturn("K24");
        when(sinhVienRepository.findById("SV002")).thenReturn(Optional.of(sv));

        MonHoc m1 = mock(MonHoc.class);
        when(m1.getMaMon()).thenReturn("M1");
        when(m1.getSoTinChi()).thenReturn(3);
        MonHoc m2 = mock(MonHoc.class);
        when(m2.getMaMon()).thenReturn("M2");
        when(m2.getSoTinChi()).thenReturn(3);
        MonHoc m3 = mock(MonHoc.class);
        when(m3.getMaMon()).thenReturn("M3");
        when(m3.getSoTinChi()).thenReturn(3);
        MonHoc m4 = mock(MonHoc.class);
        when(m4.getMaMon()).thenReturn("M4");
        when(m4.getSoTinChi()).thenReturn(3);

        ChuongTrinhDaoTao c1 = mockCtdt(nganh, khoa, m1, KhoiKienThuc.CHUYEN_NGANH, LoaiHocPhan.BAT_BUOC, 1);
        ChuongTrinhDaoTao c2 = mockCtdt(nganh, khoa, m2, KhoiKienThuc.CHUYEN_NGANH, LoaiHocPhan.TU_CHON, 2);
        ChuongTrinhDaoTao c3 = mockCtdt(nganh, khoa, m3, KhoiKienThuc.CHUYEN_NGANH, LoaiHocPhan.TU_CHON, 2);
        ChuongTrinhDaoTao c4 = mockCtdt(nganh, khoa, m4, KhoiKienThuc.DAI_CUONG, LoaiHocPhan.BAT_BUOC, 1);
        when(chuongTrinhDaoTaoRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc("CNTT", "K24"))
                .thenReturn(List.of(c1, c2, c3, c4));

        DinhMucCTDT quotaCn = new DinhMucCTDT(nganh, khoa, KhoiKienThuc.CHUYEN_NGANH, 3);
        DinhMucCTDT quotaDc = new DinhMucCTDT(nganh, khoa, KhoiKienThuc.DAI_CUONG, 0);
        when(dinhMucCTDTRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc("CNTT", "K24"))
                .thenReturn(List.of(quotaCn, quotaDc));

        BangDiemSinhVienView passM1 = mock(BangDiemSinhVienView.class);
        when(passM1.getMaMon()).thenReturn("M1");
        when(passM1.getDiemTong()).thenReturn(8.0);
        BangDiemSinhVienView failM2 = mock(BangDiemSinhVienView.class);
        when(failM2.getMaMon()).thenReturn("M2");
        when(failM2.getDiemTong()).thenReturn(2.0);
        when(bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc("SV002"))
                .thenReturn(List.of(passM1, failM2));

        LopHocPhan openLhp = mock(LopHocPhan.class);
        when(openLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.CHO_MO);
        when(openLhp.getMonHoc()).thenReturn(m3);
        DangKyHocPhan dkM3 = mock(DangKyHocPhan.class);
        when(dkM3.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(dkM3.getLopHocPhan()).thenReturn(openLhp);
        when(dangKyHocPhanRepository.findBySinhVien_MaSV("SV002")).thenReturn(List.of(dkM3));

        TienDoHocTapView view = chuongTrinhDaoTaoService.getTienDoCTDT("SV002");

        // M1 (3) + M4 (3) bat buoc + dinh muc tu chon CHUYEN_NGANH (3) = 9 TC
        assertThat(view.totalCurriculumCredits()).isEqualTo(9);
        assertThat(view.completedCredits()).isEqualTo(3);
        assertThat(view.courseStatuses())
                .containsEntry("M1", "DA_TICH_LUY")
                .containsEntry("M2", "CHUA_DAT")
                .containsEntry("M3", "DANG_HOC")
                .containsEntry("M4", "CHUA_HOC");
    }

    @Test
    @DisplayName("saveQuotas & cloneCurriculum: Chặn định mức âm và sao chép khung CTĐT sang khóa mới")
    void saveQuotasAndCloneCurriculum_validAndInvalidCases_behaveCorrectly() {
        NganhHoc nganh = new NganhHoc("CNTT", "CNTT", null);
        KhoaNhapHoc k24 = new KhoaNhapHoc("K24", 2024);
        KhoaNhapHoc k25 = new KhoaNhapHoc("K25", 2025);
        when(nganhHocRepository.findById("CNTT")).thenReturn(Optional.of(nganh));
        when(khoaNhapHocRepository.findById("K25")).thenReturn(Optional.of(k25));

        assertThatThrownBy(() -> chuongTrinhDaoTaoService.saveQuotas("CNTT", "K25", -1, 0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("ctdt.quota.negative");

        MonHoc m1 = new MonHoc("M1", "Môn 1", 3, 2, 1, 0.2, 0.2, 0.6, true, null);
        ChuongTrinhDaoTao src = new ChuongTrinhDaoTao(nganh, k24, m1, KhoiKienThuc.DAI_CUONG, LoaiHocPhan.BAT_BUOC, 1);
        when(chuongTrinhDaoTaoRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc("CNTT", "K24"))
                .thenReturn(List.of(src));
        when(chuongTrinhDaoTaoRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc("CNTT", "K25"))
                .thenReturn(List.of());
        when(dinhMucCTDTRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc("CNTT", "K24"))
                .thenReturn(List.of());

        int copied = chuongTrinhDaoTaoService.cloneCurriculum("CNTT", "K24", "K25");
        assertThat(copied).isEqualTo(1);
        verify(chuongTrinhDaoTaoRepository).saveAll(anyList());
    }

    private ChuongTrinhDaoTao mockCtdt(NganhHoc nganh, KhoaNhapHoc khoa, MonHoc mon,
                                       KhoiKienThuc khoi, LoaiHocPhan loai, int hk) {
        ChuongTrinhDaoTao c = mock(ChuongTrinhDaoTao.class);
        when(c.getNganhHoc()).thenReturn(nganh);
        when(c.getKhoaNhapHoc()).thenReturn(khoa);
        when(c.getMonHoc()).thenReturn(mon);
        when(c.getKhoiKienThuc()).thenReturn(khoi);
        when(c.getLoaiHocPhan()).thenReturn(loai);
        when(c.getHocKyChuan()).thenReturn(hk);
        return c;
    }
}
