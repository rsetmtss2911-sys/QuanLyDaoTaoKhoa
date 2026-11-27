package com.qldtkhoa.qldtkhoa_app.security;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.config.SecurityConfig;
import com.qldtkhoa.qldtkhoa_app.controller.admin.LopHocPhanAdminController;
import com.qldtkhoa.qldtkhoa_app.controller.admin.NhatKyHeThongController;
import com.qldtkhoa.qldtkhoa_app.controller.admin.QuyetDinhHocVuAdminController;
import com.qldtkhoa.qldtkhoa_app.controller.giangvien.GiangVienCoVanController;
import com.qldtkhoa.qldtkhoa_app.controller.giangvien.GiangVienLopHocPhanController;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanDetailView;
import com.qldtkhoa.qldtkhoa_app.dto.NhatKyRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienPreviewDto;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.service.AuditService;
import com.qldtkhoa.qldtkhoa_app.service.ChuongTrinhDaoTaoService;
import com.qldtkhoa.qldtkhoa_app.service.DangKyHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.HocLucService;
import com.qldtkhoa.qldtkhoa_app.service.LopHanhChinhService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.MonHocService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhHocVuService;
import com.qldtkhoa.qldtkhoa_app.service.QuyetDinhSyncService;
import com.qldtkhoa.qldtkhoa_app.service.SinhVienService;
import com.qldtkhoa.qldtkhoa_app.util.CsvExportUtils;
import com.qldtkhoa.qldtkhoa_app.util.StatusBadge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        NhatKyHeThongController.class,
        LopHocPhanAdminController.class,
        GiangVienLopHocPhanController.class,
        GiangVienCoVanController.class,
        QuyetDinhHocVuAdminController.class
})
@Import({SecurityConfig.class, StatusBadge.class})
@EnableConfigurationProperties(AcademicPolicy.class)
class Phase4WorkflowWebMvcTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @MockitoBean
    private AuditService auditService;
    @MockitoBean
    private LopHocPhanService lopHocPhanService;
    @MockitoBean
    private MonHocService monHocService;
    @MockitoBean
    private HocKyService hocKyService;
    @MockitoBean
    private NhanSuService nhanSuService;
    @MockitoBean
    private DiemService diemService;
    @MockitoBean
    private LopHanhChinhService lopHanhChinhService;
    @MockitoBean
    private SinhVienService sinhVienService;
    @MockitoBean
    private HocLucService hocLucService;
    @MockitoBean
    private ChuongTrinhDaoTaoService ctdtService;
    @MockitoBean
    private DangKyHocPhanService dangKyHocPhanService;
    @MockitoBean
    private QuyetDinhHocVuService quyetDinhHocVuService;
    @MockitoBean
    private QuyetDinhSyncService quyetDinhSyncService;
    @MockitoBean
    private Clock clock;

    private LopHocPhanDetailView sampleLopDetail() {
        return new LopHocPhanDetailView(
                "INT2204_1", "INT2204", "Lập trình OOP", 3, 2, 1,
                0.2, 0.3, 0.5, "HK1_2025", "Học kỳ 1", 2025,
                "GV01", "TS. Nguyễn Văn A", 50, TrangThaiLop.DA_CHOT_DANH_SACH);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("CSP Header: HTML response bật script-src 'self' và không có 'unsafe-inline'")
    void htmlResponse_containsStrictCspHeaderAndResolvedMessages() throws Exception {
        when(auditService.search(anyString(), any(), any(), any())).thenReturn(Page.empty());
        when(auditService.findDistinctDoiTuong()).thenReturn(List.of());
        when(auditService.findDistinctHanhDong()).thenReturn(List.of());

        MvcResult result = mockMvc.perform(get("/admin/nhat-ky"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("'unsafe-inline'"))))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).doesNotContain("??");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/nhat-ky/export-csv: ADMIN tải CSV kèm BOM và X-Content-Type-Options: nosniff")
    void auditExportCsv_adminRole_returnsSafeCsv() throws Exception {
        NhatKyRow row = new NhatKyRow(
                1L, LocalDateTime.of(2026, 3, 1, 9, 0), "admin@edu.vn",
                "EDIT", "KetQua", "SV01", "7.0", "8.0", "=FORMULA_TEST");
        when(auditService.findForExport(anyString(), any(), any()))
                .thenReturn(List.of(row));

        MvcResult res = mockMvc.perform(get("/admin/nhat-ky/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        assertThat(body).startsWith("\uFEFF");
        assertThat(body).contains("'=FORMULA_TEST");
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("GET /admin/nhat-ky/export-csv: PHONG_DAO_TAO bị chặn 403")
    void auditExportCsv_phongDaoTaoRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/nhat-ky/export-csv"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("GET /admin/lop-hoc-phan/{id}/diem/export-csv: PHONG_DAO_TAO xuất bảng điểm CSV hợp lệ")
    void adminSectionExportCsv_phongDaoTao_ok() throws Exception {
        when(lopHocPhanService.getDetailView("INT2204_1")).thenReturn(sampleLopDetail());
        when(diemService.exportGradeSheetCsv("INT2204_1")).thenReturn(
                CsvExportUtils.csvResponse("bang-diem-INT2204_1.csv", List.of("MaSV"), List.of(List.of("SV01"))));

        mockMvc.perform(get("/admin/lop-hoc-phan/INT2204_1/diem/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("GET /giang-vien/lop-hoc-phan/{id}/diem/export-csv: Kiểm tra ownership giảng viên (200 vs 403)")
    void lecturerExportCsv_enforcesOwnership() throws Exception {
        AppUserPrincipal owner = new AppUserPrincipal("gv01@edu.vn", "x", "GIANG_VIEN", true, "TS. A", "GV01", null, false);
        when(lopHocPhanService.getAssignedDetailView("INT2204_1", "GV01")).thenReturn(sampleLopDetail());
        when(diemService.exportGradeSheetCsv("INT2204_1")).thenReturn(
                CsvExportUtils.csvResponse("bang-diem.csv", List.of("MaSV"), List.of(List.of("SV01"))));

        mockMvc.perform(get("/giang-vien/lop-hoc-phan/INT2204_1/diem/export-csv").with(user(owner)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")));

        AppUserPrincipal intruder = new AppUserPrincipal("gv02@edu.vn", "x", "GIANG_VIEN", true, "TS. B", "GV02", null, false);
        when(lopHocPhanService.getAssignedDetailView("INT2204_1", "GV02"))
                .thenThrow(new AccessDeniedException("Không phụ trách lớp"));

        mockMvc.perform(get("/giang-vien/lop-hoc-phan/INT2204_1/diem/export-csv").with(user(intruder)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("GET /admin/quyet-dinh/preview-student & POST /sync-due: JSON preview và CSRF + redirect")
    void quyetDinhPreviewAndSyncDue_workCorrectly() throws Exception {
        SinhVienPreviewDto preview = new SinhVienPreviewDto(
                true, "SV01", "Trần Thị Mai", "CNTT2023A", "DANG_HOC", "Đang học",
                LocalDate.of(2025, 9, 1),
                List.of(new SinhVienPreviewDto.OptionDto("BAO_LUU", "Bảo lưu")));
        when(quyetDinhHocVuService.previewStudent("SV01")).thenReturn(preview);

        mockMvc.perform(get("/admin/quyet-dinh/preview-student").param("maSV", "SV01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.trangThai").value("DANG_HOC"))
                .andExpect(jsonPath("$.allowedTypes[0].code").value("BAO_LUU"));

        mockMvc.perform(post("/admin/quyet-dinh/sync-due"))
                .andExpect(status().isForbidden());

        when(quyetDinhSyncService.syncDueDecisions()).thenReturn(2);
        mockMvc.perform(post("/admin/quyet-dinh/sync-due").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/quyet-dinh"));
    }

    @Test
    @DisplayName("GET /giang-vien/co-van/sinh-vien/{maSV}: Chặn IDOR khi giảng viên không phải cố vấn")
    void coVanStudentDetail_idor_returnsForbidden() throws Exception {
        AppUserPrincipal intruder = new AppUserPrincipal("gv02@edu.vn", "x", "GIANG_VIEN", true, "TS. B", "GV02", null, true);
        when(sinhVienService.getAdvisedStudentDetail("SV99", "GV02"))
                .thenThrow(new AccessDeniedException("Không phải cố vấn"));

        mockMvc.perform(get("/giang-vien/co-van/sinh-vien/SV99").with(user(intruder)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /giang-vien/lop-hoc-phan/{id}/diem: Điểm ngoài [0,10] trả HTTP 200, giữ nguyên input và không sót ??key??")
    void lecturerSaveGrades_invalidScore_returns200PreservingInputsAndNoUnresolvedKeys() throws Exception {
        AppUserPrincipal owner = new AppUserPrincipal("gv01@edu.vn", "x", "GIANG_VIEN", true, "TS. A", "GV01", null, false);
        when(lopHocPhanService.getDetailView("INT2204_1")).thenReturn(sampleLopDetail());
        when(diemService.getGradeSheet("INT2204_1")).thenReturn(List.of(
                new DiemRow("SV01", "Trần Thị Mai", "CNTT2023A", TrangThaiDangKy.HOP_LE,
                        7.0, 8.0, 9.0, 8.3, 3.5, "B+", false, null)));

        MvcResult result = mockMvc.perform(post("/giang-vien/lop-hoc-phan/INT2204_1/diem")
                        .with(user(owner))
                        .with(csrf())
                        .param("entries[0].maSV", "SV01")
                        .param("entries[0].diemQuaTrinh", "15.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("giangvien/lop-hoc-phan/diem"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("15.0");
        assertThat(html).doesNotContain("??");
    }
}
