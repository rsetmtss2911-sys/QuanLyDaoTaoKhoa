package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinhVienBangDiemControllerTest {

    @Mock
    private DiemService diemService;

    @InjectMocks
    private SinhVienBangDiemController controller;

    @Test
    @DisplayName("viewTranscript: Lấy bảng điểm của sinh viên hiện tại và gán đầy đủ model")
    void viewTranscript_populatesModelCorrectly() {
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaSV()).thenReturn("SV001");

        BangDiemSinhVienResponse response = new BangDiemSinhVienResponse(
                Collections.emptyMap(),
                45,
                3.45,
                "Giỏi"
        );
        when(diemService.getTranscriptView("SV001")).thenReturn(response);

        Model model = new ConcurrentModel();
        String view = controller.viewTranscript(principal, model);

        assertThat(view).isEqualTo("sinhvien/bang-diem");
        assertThat(model.getAttribute("byTerm")).isEqualTo(Collections.emptyMap());
        assertThat(model.getAttribute("tongTCTichLuy")).isEqualTo(45);
        assertThat(model.getAttribute("cpa4")).isEqualTo(3.45);
        assertThat(model.getAttribute("xepLoai")).isEqualTo("Giỏi");

        verify(diemService).getTranscriptView("SV001");
    }
}
