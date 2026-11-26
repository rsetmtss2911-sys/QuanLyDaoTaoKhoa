package com.qldtkhoa.qldtkhoa_app.controller.giangvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
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
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

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
    @DisplayName("saveGrades: Khi có lỗi validation điểm số, render lại view bảng điểm và giữ nguyên dữ liệu đã nhập")
    void saveGrades_hasValidationErrors_returnsGradeViewPreservingInputs() {
        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("SV001");
        entry.setDiemQuaTrinh(8.5);
        entry.setDiemCuoiKy(12.0);
        BangDiemLopForm form = new BangDiemLopForm();
        form.setEntries(List.of(entry));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        Model model = new ConcurrentModel();

        when(lopHocPhanService.getWithDetailsOrThrow("LHP001")).thenReturn(mock(LopHocPhan.class));
        when(diemService.getGradeSheet("LHP001")).thenReturn(List.of(
                new DiemRow("SV001", "Nguyễn Văn A", "K66", TrangThaiDangKy.HOP_LE, 7.0, null, null, null, null, null, false)
        ));

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, model, redirect);

        assertThat(view).isEqualTo("giangvien/lop-hoc-phan/diem");
        assertThat(model.getAttribute("flash")).isNotNull();
        @SuppressWarnings("unchecked")
        List<DiemRow> merged = (List<DiemRow>) model.getAttribute("rows");
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).diemQuaTrinh()).isEqualTo(8.5);
        assertThat(merged.get(0).diemCuoiKy()).isEqualTo(12.0);
        verify(diemService, never()).saveGradeSheet(any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("saveGrades: Khi vi phạm quy tắc nghiệp vụ, render lại view bảng điểm với flash lỗi")
    void saveGrades_businessRuleException_returnsGradeViewWithFlash() {
        BangDiemLopForm form = new BangDiemLopForm();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaNS()).thenReturn("GV001");
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        Model model = new ConcurrentModel();

        doThrow(new BusinessRuleException("grade.classNotLocked"))
                .when(diemService).saveGradeSheet("LHP001", form, "GV001", false);
        when(lopHocPhanService.getWithDetailsOrThrow("LHP001")).thenReturn(mock(LopHocPhan.class));
        when(diemService.getGradeSheet("LHP001")).thenReturn(List.of());

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, model, redirect);

        assertThat(view).isEqualTo("giangvien/lop-hoc-phan/diem");
        assertThat(model.getAttribute("flash")).isNotNull();
    }

    @Test
    @DisplayName("saveGrades: Bỏ qua phần tử null trong entries khi render lại view lỗi (tránh NPE)")
    void saveGrades_hasValidationErrorsWithNullEntry_ignoresNullAndPreservesValidInputs() {
        DiemEntryForm entry = new DiemEntryForm();
        entry.setMaSV("SV001");
        entry.setDiemQuaTrinh(9.0);
        BangDiemLopForm form = new BangDiemLopForm();
        form.setEntries(Arrays.asList(null, entry));

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        Model model = new ConcurrentModel();

        when(lopHocPhanService.getWithDetailsOrThrow("LHP001")).thenReturn(mock(LopHocPhan.class));
        when(diemService.getGradeSheet("LHP001")).thenReturn(List.of(
                new DiemRow("SV001", "Nguyễn Văn A", "K66", TrangThaiDangKy.HOP_LE, 7.0, null, null, null, null, null, false)
        ));

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, model, redirect);

        assertThat(view).isEqualTo("giangvien/lop-hoc-phan/diem");
        @SuppressWarnings("unchecked")
        List<DiemRow> merged = (List<DiemRow>) model.getAttribute("rows");
        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).diemQuaTrinh()).isEqualTo(9.0);
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
        Model model = new ConcurrentModel();

        String view = controller.saveGrades("LHP001", form, bindingResult, principal, model, redirect);

        assertThat(view).isEqualTo("redirect:/giang-vien/lop-hoc-phan/{maLHP}/diem");
        verify(diemService).saveGradeSheet("LHP001", form, "GV001", false);
        verify(redirect).addAttribute("maLHP", "LHP001");
        verify(redirect).addFlashAttribute(eq("flash"), any());
    }
}
