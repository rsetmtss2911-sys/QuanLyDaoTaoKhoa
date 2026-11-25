package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.SinhVienForm;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
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
class SinhVienAdminControllerTest {

    @Mock
    private SinhVienService sinhVienService;

    @Mock
    private LopHanhChinhService lopHanhChinhService;

    @Mock
    private QuyetDinhHocVuService quyetDinhHocVuService;

    @Mock
    private DangKyHocPhanService dangKyHocPhanService;

    @Mock
    private com.qldtkhoa.qldtkhoa_app.service.DiemService diemService;

    @InjectMocks
    private SinhVienAdminController controller;

    @Test
    @DisplayName("gioiTinhs model attribute should contain all GioiTinh enum values")
    void testGioiTinhsModelAttribute() {
        GioiTinh[] values = controller.gioiTinhs();
        assertThat(values).containsExactly(GioiTinh.NAM, GioiTinh.NU, GioiTinh.KHAC);
    }

    @Test
    @DisplayName("detail should invoke getWithDetailsOrThrow and populate model")
    void testDetailLoadsWithDetails() {
        String maSV = "SV2024001";
        SinhVien mockSv = mock(SinhVien.class);
        when(sinhVienService.getWithDetailsOrThrow(maSV)).thenReturn(mockSv);
        when(quyetDinhHocVuService.findBySinhVien(maSV)).thenReturn(Collections.emptyList());
        when(dangKyHocPhanService.findBySinhVien(maSV)).thenReturn(Collections.emptyList());
        when(diemService.getTranscriptView(maSV)).thenReturn(new com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse(Collections.emptyMap(), 0, null, "—"));

        Model model = new ConcurrentModel();
        String view = controller.detail(maSV, model);

        assertThat(view).isEqualTo("admin/sinh-vien/detail");
        assertThat(model.getAttribute("sv")).isSameAs(mockSv);
        assertThat(model.getAttribute("quyetDinhs")).isNotNull();
        assertThat(model.getAttribute("dangKys")).isNotNull();
        assertThat(model.getAttribute("byTerm")).isNotNull();
        assertThat(model.getAttribute("tongTCTichLuy")).isEqualTo(0);
        verify(sinhVienService).getWithDetailsOrThrow(maSV);
    }

    @Test
    @DisplayName("detail should populate model when student has academic decisions")
    void testDetailLoadsWithAcademicDecisions() {
        String maSV = "SV65_06";
        SinhVien mockSv = mock(SinhVien.class);
        when(sinhVienService.getWithDetailsOrThrow(maSV)).thenReturn(mockSv);
        com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow qdRow = new com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow(
                "QD_2025_002",
                maSV,
                "Bùi Hữu Trí",
                "K65_CNTT",
                com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh.DINH_CHI,
                java.time.LocalDate.of(2026, 1, 12),
                "Đình chỉ học tập 1 học kỳ"
        );
        when(quyetDinhHocVuService.findBySinhVien(maSV)).thenReturn(java.util.List.of(qdRow));
        when(dangKyHocPhanService.findBySinhVien(maSV)).thenReturn(Collections.emptyList());
        when(diemService.getTranscriptView(maSV)).thenReturn(new com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse(Collections.emptyMap(), 0, null, "—"));

        Model model = new ConcurrentModel();
        String view = controller.detail(maSV, model);

        assertThat(view).isEqualTo("admin/sinh-vien/detail");
        assertThat(model.getAttribute("sv")).isSameAs(mockSv);
        @SuppressWarnings("unchecked")
        java.util.List<com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow> decisions =
                (java.util.List<com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow>) model.getAttribute("quyetDinhs");
        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).maQuyetDinh()).isEqualTo("QD_2025_002");
    }

    @Test
    @DisplayName("editForm should populate form, lopHanhChinhs and gioiTinhs")
    void testEditFormPopulatesModel() {
        String maSV = "SV2024001";
        SinhVienForm form = new SinhVienForm();
        form.setMaSV(maSV);
        when(sinhVienService.getForm(maSV)).thenReturn(form);
        when(lopHanhChinhService.findAll()).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.editForm(maSV, model);

        assertThat(view).isEqualTo("admin/sinh-vien/form");
        assertThat(model.getAttribute("form")).isSameAs(form);
        assertThat(model.getAttribute("isEdit")).isEqualTo(true);
        assertThat(model.getAttribute("lopHanhChinhs")).isNotNull();
        assertThat(model.getAttribute("gioiTinhs")).isEqualTo(GioiTinh.values());
    }

    @Test
    @DisplayName("newForm should populate lopHanhChinhs and gioiTinhs")
    void testNewFormPopulatesModel() {
        when(lopHanhChinhService.findAll()).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.newForm(null, model);

        assertThat(view).isEqualTo("admin/sinh-vien/form");
        assertThat(model.getAttribute("form")).isNotNull();
        assertThat(model.getAttribute("isEdit")).isEqualTo(false);
        assertThat(model.getAttribute("lopHanhChinhs")).isNotNull();
        assertThat(model.getAttribute("gioiTinhs")).isEqualTo(GioiTinh.values());
    }
}
