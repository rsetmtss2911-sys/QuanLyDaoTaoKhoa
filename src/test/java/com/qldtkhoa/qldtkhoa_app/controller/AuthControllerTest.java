package com.qldtkhoa.qldtkhoa_app.controller;

import com.qldtkhoa.qldtkhoa_app.dto.ChangePasswordForm;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.service.TaiKhoanService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TaiKhoanService taiKhoanService;

    @Mock
    private Environment environment;

    @InjectMocks
    private AuthController controller;

    @Test
    @DisplayName("login: Người dùng chưa đăng nhập thì trả về view auth/login và ẩn tài khoản mẫu khi không ở profile dev/local")
    void login_anonymous_returnsLoginView() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        Model model1 = new ConcurrentModel();
        String view = controller.login(auth, model1);
        assertThat(view).isEqualTo("auth/login");
        assertThat(model1.getAttribute("showDemoCredentials")).isEqualTo(false);

        Model model2 = new ConcurrentModel();
        String viewNull = controller.login(null, model2);
        assertThat(viewNull).isEqualTo("auth/login");
    }

    @Test
    @DisplayName("login: Người dùng đã xác thực thì redirect về trang chủ")
    void login_authenticated_redirectsHome() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("admin@vnu.edu.vn");

        String view = controller.login(auth, new ConcurrentModel());
        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    @DisplayName("changePasswordForm: Cung cấp form rỗng và view auth/change-password")
    void changePasswordForm_populatesModel() {
        Model model = new ConcurrentModel();
        String view = controller.changePasswordForm(model);

        assertThat(view).isEqualTo("auth/change-password");
        assertThat(model.getAttribute("form")).isNotNull();
    }

    @Test
    @DisplayName("changePassword: Có lỗi validation thì trả về view auth/change-password")
    void changePassword_validationErrors_returnsView() {
        ChangePasswordForm form = new ChangePasswordForm();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        Authentication auth = mock(Authentication.class);
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        String view = controller.changePassword(form, result, auth, null, redirect);

        assertThat(view).isEqualTo("auth/change-password");
        verifyNoInteractions(taiKhoanService);
    }

    @Test
    @DisplayName("changePassword: Hợp lệ thì gọi service và redirect về trang chủ với flash message")
    void changePassword_success_redirectsHome() {
        ChangePasswordForm form = new ChangePasswordForm();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@vnu.edu.vn");
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        String view = controller.changePassword(form, result, auth, request, redirect);

        assertThat(view).isEqualTo("redirect:/");
        verify(taiKhoanService).changePassword("user@vnu.edu.vn", form, null);
        verify(redirect).addFlashAttribute(eq("flash"), any());
    }

    @Test
    @DisplayName("changePassword: Sai mật khẩu cũ ném BusinessRuleException thì reject lỗi vào form")
    void changePassword_businessRuleException_rejectsError() {
        ChangePasswordForm form = new ChangePasswordForm();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@vnu.edu.vn");
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        doThrow(new BusinessRuleException("auth.password.oldMismatch", "Mật khẩu cũ không đúng"))
                .when(taiKhoanService).changePassword("user@vnu.edu.vn", form, null);

        String view = controller.changePassword(form, result, auth, null, redirect);

        assertThat(view).isEqualTo("auth/change-password");
        verify(result).reject(eq("auth.password.oldMismatch"), anyString());
    }
}
