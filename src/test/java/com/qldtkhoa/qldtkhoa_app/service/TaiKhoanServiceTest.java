package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.ChangePasswordForm;
import com.qldtkhoa.qldtkhoa_app.entity.TaiKhoan;
import com.qldtkhoa.qldtkhoa_app.entity.VaiTro;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.repository.TaiKhoanRepository;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.security.SessionInvalidationHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaiKhoanServiceTest {

    @Mock
    private TaiKhoanRepository taiKhoanRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionInvalidationHelper sessionInvalidationHelper;

    @InjectMocks
    private TaiKhoanService taiKhoanService;

    @Test
    @DisplayName("toggleLock: Admin không thể tự khóa chính mình")
    void toggleLock_self_throwsBusinessRuleException() {
        assertThatThrownBy(() -> taiKhoanService.toggleLock("admin@truong.edu.vn", "ADMIN@truong.edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("taikhoan.error.selfLock");
    }

    @Test
    @DisplayName("toggleLock: Không thể khóa tài khoản ADMIN hoạt động cuối cùng")
    void toggleLock_lastActiveAdmin_throwsBusinessRuleException() {
        VaiTro adminRole = new VaiTro("ADMIN", "Admin");
        TaiKhoan tk = new TaiKhoan("admin2@truong.edu.vn", "hash", adminRole, true);
        when(taiKhoanRepository.findByEmail("admin2@truong.edu.vn")).thenReturn(Optional.of(tk));
        when(taiKhoanRepository.countByVaiTro_MaVaiTroAndTrangThaiKichHoat("ADMIN", true)).thenReturn(1L);

        assertThatThrownBy(() -> taiKhoanService.toggleLock("admin2@truong.edu.vn", "admin1@truong.edu.vn"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("taikhoan.error.lastAdmin");
    }

    @Test
    @DisplayName("toggleLock: Khóa tài khoản thường sẽ thu hồi ngay phiên đăng nhập")
    void toggleLock_activeUser_locksAndExpiresSessions() {
        VaiTro svRole = new VaiTro("SINH_VIEN", "Sinh viên");
        TaiKhoan tk = new TaiKhoan("sv@student.edu.vn", "hash", svRole, true);
        when(taiKhoanRepository.findByEmail("sv@student.edu.vn")).thenReturn(Optional.of(tk));

        boolean locked = taiKhoanService.toggleLock("sv@student.edu.vn", "admin@truong.edu.vn");

        assertThat(locked).isTrue();
        assertThat(tk.getTrangThaiKichHoat()).isFalse();
        verify(taiKhoanRepository).saveAndFlush(tk);
        verify(sessionInvalidationHelper).expireUserSessions("sv@student.edu.vn");
    }

    @Test
    @DisplayName("changePassword: Cấm trùng mật khẩu hiện tại")
    void changePassword_sameAsOld_throwsBusinessRuleException() {
        VaiTro svRole = new VaiTro("SINH_VIEN", "Sinh viên");
        TaiKhoan tk = new TaiKhoan("sv@student.edu.vn", "encodedOld", svRole, true);
        when(taiKhoanRepository.findByEmail("sv@student.edu.vn")).thenReturn(Optional.of(tk));

        ChangePasswordForm form = new ChangePasswordForm();
        form.setMatKhauCu("OldPass@1");
        form.setMatKhauMoi("OldPass@1");
        form.setXacNhanMatKhau("OldPass@1");

        when(passwordEncoder.matches("OldPass@1", "encodedOld")).thenReturn(true);

        assertThatThrownBy(() -> taiKhoanService.changePassword("sv@student.edu.vn", form, "sess1"))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("messageKey")
                .isEqualTo("auth.password.sameAsOld");
    }

    @Test
    @DisplayName("AppUserPrincipal: eraseCredentials xóa password và equals/hashCode không phân biệt hoa thường email")
    void appUserPrincipal_eraseCredentialsAndCaseInsensitiveEquals() {
        AppUserPrincipal p1 = new AppUserPrincipal("Admin@Truong.Edu.VN", "bcrypt", "ADMIN", true, "Admin", "NS_ADMIN", null, false);
        AppUserPrincipal p2 = new AppUserPrincipal("admin@truong.edu.vn", "other", "ADMIN", true, "Admin", "NS_ADMIN", null, false);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());

        p1.eraseCredentials();
        assertThat(p1.getPassword()).isNull();
    }
}
