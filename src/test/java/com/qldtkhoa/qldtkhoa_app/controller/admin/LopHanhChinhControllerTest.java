package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.entity.LopHanhChinh;
import com.qldtkhoa.qldtkhoa_app.service.KhoaNhapHocService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.NganhHocService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
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
class LopHanhChinhControllerTest {

    @Mock
    private LopHanhChinhService lopHanhChinhService;

    @Mock
    private SinhVienService sinhVienService;

    @Mock
    private NganhHocService nganhHocService;

    @Mock
    private KhoaNhapHocService khoaNhapHocService;

    @Mock
    private NhanSuService nhanSuService;

    @InjectMocks
    private LopHanhChinhController controller;

    @Test
    @DisplayName("detail should call getWithDetailsOrThrow and populate sinhViens")
    void testDetailLoadsWithDetails() {
        String maLop = "CNTT-K24A";
        LopHanhChinh mockLop = mock(LopHanhChinh.class);
        when(lopHanhChinhService.getWithDetailsOrThrow(maLop)).thenReturn(mockLop);
        when(sinhVienService.findByLopHanhChinh(maLop)).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.detail(maLop, model);

        assertThat(view).isEqualTo("admin/lop-hanh-chinh/detail");
        assertThat(model.getAttribute("lop")).isSameAs(mockLop);
        assertThat(model.getAttribute("sinhViens")).isNotNull();
        verify(lopHanhChinhService).getWithDetailsOrThrow(maLop);
    }
}
