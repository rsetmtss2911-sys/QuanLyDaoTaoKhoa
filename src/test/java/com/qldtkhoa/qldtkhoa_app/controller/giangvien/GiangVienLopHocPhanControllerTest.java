package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiangVienLopHocPhanControllerTest {

    @Mock
    private LopHocPhanService lopHocPhanService;

    @Mock
    private HocKyService hocKyService;

    @Mock
    private DiemService diemService;

    @InjectMocks
    private GiangVienLopHocPhanController controller;

    @Test
    @DisplayName("saveGrades: Khi có lỗi validation điểm số, thêm flash message lỗi và redirect về trang điểm")
    void saveGrades_hasValidationErrors_redirectsWithErrorMessage() {
        BangDiemLopForm form = new BangDiemLopForm();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, redirect);

        assertThat(view).isEqualTo("redirect:/giang-vien/lop-hoc-phan/LHP001/diem");
        verify(redirect).addFlashAttribute(eq("flash"), any());
        verifyNoInteractions(diemService);
    }

    @Test
    @DisplayName("saveGrades: Khi dữ liệu hợp lệ, gọi diemService và redirect với flash message thành công")
    void saveGrades_valid_savesGradeSheetAndRedirects() {
        BangDiemLopForm form = new BangDiemLopForm();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV001");
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, redirect);

        assertThat(view).isEqualTo("redirect:/giang-vien/lop-hoc-phan/LHP001/diem");
        verify(diemService).saveGradeSheet("LHP001", form, "GV001", false);
        verify(redirect).addFlashAttribute(eq("flash"), any());
    }
}
