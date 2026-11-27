package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienForm;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.VaiTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
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
    private PasswordEncoder passwordEncoder;

    private SinhVienService sinhVienService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-26T00:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        sinhVienService = new SinhVienService(
                sinhVienRepository,
                lopHanhChinhRepository,
                taiKhoanRepository,
                vaiTroRepository,
                passwordEncoder,
                fixedClock
        );
    }

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

        when(sinhVienRepository.findByIdWithDetails(maSV)).thenReturn(Optional.of(sv));

        SinhVienForm form = sinhVienService.getForm(maSV);

        assertThat(form.getMaSV()).isEqualTo(maSV);
        assertThat(form.getHoTen()).isEqualTo("Lê Thị Hoa");
        assertThat(form.getGioiTinh()).isEqualTo(GioiTinh.NU);
        assertThat(form.getMaLopHC()).isEqualTo("CNTT2023A");
        assertThat(form.getEmail()).isEqualTo("hoa.lt@vnu.edu.vn");
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

        when(sinhVienRepository.findByIdWithDetails(maSV)).thenReturn(Optional.of(sv));

        SinhVienForm form = sinhVienService.getForm(maSV);

        assertThat(form.getMaSV()).isEqualTo(maSV);
        assertThat(form.getHoTen()).isEqualTo("Nguyễn Văn B");
        assertThat(form.getGioiTinh()).isEqualTo(GioiTinh.NAM);
        assertThat(form.getMaLopHC()).isEmpty();
        assertThat(form.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("create should reject student under 17 years old")
    void create_under17_throwsBusinessRuleException() {
        SinhVienForm form = new SinhVienForm();
        form.setMaSV("SV_YOUNG");
        form.setHoTen("Nguyễn Văn Nhỏ");
        form.setEmail("young@student.edu.vn");
        form.setNgaySinh(LocalDate.of(2012, 1, 1));
        form.setMaLopHC("K66_CNTT");

        when(sinhVienRepository.existsById("SV_YOUNG")).thenReturn(false);
        when(taiKhoanRepository.existsById("young@student.edu.vn")).thenReturn(false);

        assertThatThrownBy(() -> sinhVienService.create(form))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("sinhvien.ngaySinh.age");
    }

    @Test
    @DisplayName("delete should always reject physical deletion of student records")
    void delete_physicalDelete_throwsBusinessRuleException() {
        SinhVien sv = mock(SinhVien.class);
        when(sinhVienRepository.findById("SV2024001")).thenReturn(Optional.of(sv));

        assertThatThrownBy(() -> sinhVienService.delete("SV2024001"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("sinhvien.delete.forbidden");
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

    @Test
    @DisplayName("findForExport: Thoát wildcard LIKE và ánh xạ sang SinhVienRow không kèm Sort.by")
    void findForExport_escapesWildcardsAndMapsRows() {
        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("SV01");
        when(sv.getHoTen()).thenReturn("Nguyễn Văn A");
        when(sv.getGioiTinh()).thenReturn(GioiTinh.NAM);
        when(sv.getTrangThaiHienTai()).thenReturn(TrangThaiSinhVien.DANG_HOC);
        when(sinhVienRepository.findForExport(eq("100\\%\\_"), eq("K68_A"), eq(TrangThaiSinhVien.DANG_HOC), eq(PageRequest.of(0, 5000))))
                .thenReturn(List.of(sv));

        List<SinhVienRow> rows = sinhVienService.findForExport("100%_", "K68_A", TrangThaiSinhVien.DANG_HOC);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).maSV()).isEqualTo("SV01");
    }
}
