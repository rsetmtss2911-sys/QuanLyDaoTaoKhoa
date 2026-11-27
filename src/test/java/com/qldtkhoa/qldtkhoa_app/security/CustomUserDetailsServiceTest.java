package com.qldtkhoa.qldtkhoa_app.security;

import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.repository.LopHanhChinhRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NhanSuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;
    @Mock
    private NhanSuRepository nhanSuRepository;
    @Mock
    private SinhVienRepository sinhVienRepository;
    @Mock
    private LopHanhChinhRepository lopHanhChinhRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    private TaiKhoan mockAccount(String email, String role, boolean enabled) {
        VaiTro vt = mock(VaiTro.class);
        when(vt.getMaVaiTro()).thenReturn(role);
        TaiKhoan tk = mock(TaiKhoan.class);
        when(tk.getEmail()).thenReturn(email);
        when(tk.getMatKhau()).thenReturn("$2a$10$hash");
        when(tk.getVaiTro()).thenReturn(vt);
        when(tk.getTrangThaiKichHoat()).thenReturn(enabled);
        return tk;
    }

    @Test
    @DisplayName("loadUserByUsername: Email không tồn tại ném UsernameNotFoundException")
    void loadUserByUsername_unknownEmail_throwsUsernameNotFound() {
        when(taiKhoanRepository.findByEmail("missing@edu.vn")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@edu.vn"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserByUsername: Tài khoản bị khóa trả về isEnabled() = false")
    void loadUserByUsername_disabledAccount_setsEnabledFalse() {
        TaiKhoan tk = mockAccount("locked@edu.vn", "ADMIN", false);
        when(taiKhoanRepository.findByEmail("locked@edu.vn")).thenReturn(Optional.of(tk));
        when(nhanSuRepository.findByTaiKhoan_Email("locked@edu.vn")).thenReturn(Optional.empty());

        UserDetails details = service.loadUserByUsername("locked@edu.vn");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getUsername()).isEqualTo("locked@edu.vn");
    }

    @Test
    @DisplayName("loadUserByUsername: SINH_VIEN gán maSV và displayName, không truy vấn NhanSu")
    void loadUserByUsername_sinhVien_populatesMaSVAndDisplayName() {
        TaiKhoan tk = mockAccount("sv01@edu.vn", "SINH_VIEN", true);
        when(taiKhoanRepository.findByEmail("sv01@edu.vn")).thenReturn(Optional.of(tk));
        SinhVien sv = mock(SinhVien.class);
        when(sv.getMaSV()).thenReturn("SV001");
        when(sv.getHoTen()).thenReturn("Trần Thị Mai");
        when(sinhVienRepository.findByTaiKhoan_Email("sv01@edu.vn")).thenReturn(Optional.of(sv));

        AppUserPrincipal principal = (AppUserPrincipal) service.loadUserByUsername("sv01@edu.vn");

        assertThat(principal.getMaSV()).isEqualTo("SV001");
        assertThat(principal.getMaNS()).isNull();
        assertThat(principal.getDisplayName()).isEqualTo("Trần Thị Mai");
        assertThat(principal.getRole()).isEqualTo("SINH_VIEN");
        assertThat(principal.isAdvisor()).isFalse();
        verifyNoInteractions(nhanSuRepository, lopHanhChinhRepository);
    }

    @Test
    @DisplayName("loadUserByUsername: GIANG_VIEN có lớp cố vấn gán isAdvisor = true")
    void loadUserByUsername_giangVienAdvisor_setsAdvisorTrue() {
        TaiKhoan tk = mockAccount("gv01@edu.vn", "GIANG_VIEN", true);
        when(taiKhoanRepository.findByEmail("gv01@edu.vn")).thenReturn(Optional.of(tk));
        NhanSu ns = mock(NhanSu.class);
        when(ns.getMaNS()).thenReturn("GV01");
        when(ns.getHoTen()).thenReturn("TS. Nguyễn Văn A");
        when(nhanSuRepository.findByTaiKhoan_Email("gv01@edu.vn")).thenReturn(Optional.of(ns));
        when(lopHanhChinhRepository.existsByCoVan_MaNS("GV01")).thenReturn(true);

        AppUserPrincipal principal = (AppUserPrincipal) service.loadUserByUsername("gv01@edu.vn");

        assertThat(principal.getMaNS()).isEqualTo("GV01");
        assertThat(principal.getMaSV()).isNull();
        assertThat(principal.getDisplayName()).isEqualTo("TS. Nguyễn Văn A");
        assertThat(principal.isAdvisor()).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername: GIANG_VIEN không phụ trách lớp nào gán isAdvisor = false")
    void loadUserByUsername_giangVienNonAdvisor_setsAdvisorFalse() {
        TaiKhoan tk = mockAccount("gv02@edu.vn", "GIANG_VIEN", true);
        when(taiKhoanRepository.findByEmail("gv02@edu.vn")).thenReturn(Optional.of(tk));
        NhanSu ns = mock(NhanSu.class);
        when(ns.getMaNS()).thenReturn("GV02");
        when(ns.getHoTen()).thenReturn("ThS. Lê Văn B");
        when(nhanSuRepository.findByTaiKhoan_Email("gv02@edu.vn")).thenReturn(Optional.of(ns));
        when(lopHanhChinhRepository.existsByCoVan_MaNS("GV02")).thenReturn(false);

        AppUserPrincipal principal = (AppUserPrincipal) service.loadUserByUsername("gv02@edu.vn");

        assertThat(principal.isAdvisor()).isFalse();
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_GIANG_VIEN");
    }

    @Test
    @DisplayName("loadUserByUsername: ADMIN và PHONG_DAO_TAO dùng hồ sơ NhanSu và giữ role đúng")
    void loadUserByUsername_adminAndPdt_populatesNhanSuWithoutAdvisorCheck() {
        TaiKhoan tk = mockAccount("pdt@edu.vn", "PHONG_DAO_TAO", true);
        when(taiKhoanRepository.findByEmail("pdt@edu.vn")).thenReturn(Optional.of(tk));
        NhanSu ns = mock(NhanSu.class);
        when(ns.getMaNS()).thenReturn("PDT01");
        when(ns.getHoTen()).thenReturn("Chuyên viên ĐT");
        when(nhanSuRepository.findByTaiKhoan_Email("pdt@edu.vn")).thenReturn(Optional.of(ns));

        AppUserPrincipal principal = (AppUserPrincipal) service.loadUserByUsername("pdt@edu.vn");

        assertThat(principal.getRole()).isEqualTo("PHONG_DAO_TAO");
        assertThat(principal.getMaNS()).isEqualTo("PDT01");
        assertThat(principal.isAdvisor()).isFalse();
        verifyNoInteractions(lopHanhChinhRepository);
    }
}
