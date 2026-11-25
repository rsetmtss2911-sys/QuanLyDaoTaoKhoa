package com.qldtkhoa.qldtkhoa_app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler(messageSource);
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    @DisplayName("handleBusinessRule should return error/400 and populate attributes")
    void testHandleBusinessRule() {
        when(messageSource.getMessage(eq("reg.error.scheduleConflict"), any(), anyString(), any(Locale.class)))
                .thenReturn("Trùng thời khóa biểu với lớp học phần khác.");

        Model model = new ConcurrentModel();
        BusinessRuleException ex = new BusinessRuleException("reg.error.scheduleConflict");

        String view = exceptionHandler.handleBusinessRule(ex, request, model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("messageKey")).isEqualTo("reg.error.scheduleConflict");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Trùng thời khóa biểu với lớp học phần khác.");
        assertThat(model.getAttribute("currentPath")).isEqualTo("/test-uri");
    }

    @Test
    @DisplayName("handleValidation should return error/400 and populate common.invalidInput")
    void testHandleValidation() {
        when(messageSource.getMessage(eq("common.invalidInput"), any(), anyString(), any(Locale.class)))
                .thenReturn("Dữ liệu biểu mẫu không hợp lệ.");

        Model model = new ConcurrentModel();
        BindException ex = new BindException(new Object(), "target");

        String view = exceptionHandler.handleValidation(ex, request, model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("messageKey")).isEqualTo("common.invalidInput");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Dữ liệu biểu mẫu không hợp lệ.");
        assertThat(model.getAttribute("currentPath")).isEqualTo("/test-uri");
    }

    @Test
    @DisplayName("handleNotFound should return error/404 and populate attributes")
    void testHandleNotFound() {
        when(messageSource.getMessage(eq("sinhvien.notFound"), any(), anyString(), any(Locale.class)))
                .thenReturn("Không tìm thấy sinh viên.");

        Model model = new ConcurrentModel();
        ResourceNotFoundException ex = new ResourceNotFoundException("sinhvien.notFound", "SV001");

        String view = exceptionHandler.handleNotFound(ex, request, model);

        assertThat(view).isEqualTo("error/404");
        assertThat(model.getAttribute("messageKey")).isEqualTo("sinhvien.notFound");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Không tìm thấy sinh viên.");
        assertThat(model.getAttribute("currentPath")).isEqualTo("/test-uri");
    }

    @Test
    @DisplayName("handleUnexpected should return error/500 and populate errorRef and currentPath")
    void testHandleUnexpected() {
        Model model = new ConcurrentModel();
        Exception ex = new RuntimeException("Unexpected error");

        String view = exceptionHandler.handleUnexpected(ex, request, model);

        assertThat(view).isEqualTo("error/500");
        assertThat(model.getAttribute("errorRef")).isNotNull();
        assertThat(model.getAttribute("currentPath")).isEqualTo("/test-uri");
    }

    @Test
    @DisplayName("handleDuplicate should return error/409 and populate attributes")
    void testHandleDuplicate() {
        when(messageSource.getMessage(eq("khoa.duplicate"), any(), anyString(), any(Locale.class)))
                .thenReturn("Mã khoa đã tồn tại.");

        Model model = new ConcurrentModel();
        DuplicateResourceException ex = new DuplicateResourceException("khoa.duplicate", "CNTT");

        String view = exceptionHandler.handleDuplicate(ex, request, model);

        assertThat(view).isEqualTo("error/409");
        assertThat(model.getAttribute("messageKey")).isEqualTo("khoa.duplicate");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Mã khoa đã tồn tại.");
        assertThat(model.getAttribute("currentPath")).isEqualTo("/test-uri");
    }
}

