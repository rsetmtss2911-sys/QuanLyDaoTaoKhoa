package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienForm;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.VaiTroRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SinhVienServiceTest {

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;

    @Mock
    private QuyetDinhHocVuRepository quyetDinhHocVuRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SinhVienService sinhVienService;

    @Test
    @DisplayName("getForm should return populated form with eager details")
    void testGetFormWithFullDetails() {
        String maSV = "SV2024001";
        SinhVien sv = mock(SinhVien.class);
        TaiKhoan tk = mock(TaiKhoan.class);
        LopHanhChinh lhc = mock(LopHanhChinh.class);

        when(sv.getMaSV()).thenReturn(maSV);
        when(sv.getHoTen()).thenReturn("Lê Thị Hoa");
        when(sv.getNgaySinh()).thenReturn(LocalDate.of(2005, 8, 15));
        when(sv.getGioiTinh()).thenReturn(GioiTinh.NU);
        when(sv.getLopHanhChinh()).thenReturn(lhc);
        when(lhc.getMaLopHC()).thenReturn("CNTT2023A");
        when(sv.getTaiKhoan()).thenReturn(tk);
        when(tk.getEmail()).thenReturn("hoa.lt@vnu.edu.vn");
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);

        when(sinhVienRepository.findByIdWithDetails(maSV)).thenReturn(Optional.of(sv));

        SinhVienForm form = sinhVienService.getForm(maSV);

        assertThat(form.getMaSV()).isEqualTo(maSV);
        assertThat(form.getHoTen()).isEqualTo("Lê Thị Hoa");
        assertThat(form.getGioiTinh()).isEqualTo(GioiTinh.NU);
        assertThat(form.getMaLopHC()).isEqualTo("CNTT2023A");
        assertThat(form.getEmail()).isEqualTo("hoa.lt@vnu.edu.vn");
        assertThat(form.getTrangThaiHienTai()).isEqualTo(TrangThaiSinhVien.DANG_HOC);
    }

    @Test
    @DisplayName("getForm should handle null associations safely")
    void testGetFormWithNullAssociations() {
        String maSV = "SV2024002";
        SinhVien sv = mock(SinhVien.class);

        when(sv.getMaSV()).thenReturn(maSV);
        when(sv.getHoTen()).thenReturn("Nguyễn Văn B");
        when(sv.getNgaySinh()).thenReturn(null);
        when(sv.getGioiTinh()).thenReturn(null);
        when(sv.getLopHanhChinh()).thenReturn(null);
        when(sv.getTaiKhoan()).thenReturn(null);
        when(sv.getTrangThaiHienTai()).thenReturn(null);

        when(sinhVienRepository.findByIdWithDetails(maSV)).thenReturn(Optional.of(sv));

        SinhVienForm form = sinhVienService.getForm(maSV);

        assertThat(form.getMaSV()).isEqualTo(maSV);
        assertThat(form.getHoTen()).isEqualTo("Nguyễn Văn B");
        assertThat(form.getGioiTinh()).isEqualTo(GioiTinh.NAM);
        assertThat(form.getMaLopHC()).isEmpty();
        assertThat(form.getEmail()).isEmpty();
        assertThat(form.getTrangThaiHienTai()).isEqualTo(TrangThaiSinhVien.DANG_HOC);
    }

    @Test
    @DisplayName("toRow should safely map entity with null associations")
    void testToRowWithNullAssociations() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("SV999999");
        when(sv.getHoTen()).thenReturn("Trần Văn Test");
        when(sv.getTaiKhoan()).thenReturn(null);
        when(sv.getGioiTinh()).thenReturn(GioiTinh.NAM);
        when(sv.getNgaySinh()).thenReturn(null);
        when(sv.getLopHanhChinh()).thenReturn(null);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);

        SinhVienRow row = sinhVienService.toRow(sv);

        assertThat(row.maSV()).isEqualTo("SV999999");
        assertThat(row.hoTen()).isEqualTo("Trần Văn Test");
        assertThat(row.email()).isEmpty();
        assertThat(row.maLopHC()).isEmpty();
        assertThat(row.tenLopHC()).isEmpty();
    }
}
