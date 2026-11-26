package com.qldtkhoa.qldtkhoa_app.security;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.config.SecurityConfig;
import com.qldtkhoa.qldtkhoa_app.controller.ErrorViewController;
import com.qldtkhoa.qldtkhoa_app.controller.admin.LopHocPhanAdminController;
import com.qldtkhoa.qldtkhoa_app.controller.admin.NhatKyHeThongController;
import com.qldtkhoa.qldtkhoa_app.controller.admin.TaiKhoanAdminController;
import com.qldtkhoa.qldtkhoa_app.service.AuditService;
import com.qldtkhoa.qldtkhoa_app.service.DiemService;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.MonHocService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import com.qldtkhoa.qldtkhoa_app.service.TaiKhoanService;
import com.qldtkhoa.qldtkhoa_app.service.VaiTroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        ErrorViewController.class,
        TaiKhoanAdminController.class,
        LopHocPhanAdminController.class,
        NhatKyHeThongController.class
})
@Import(SecurityConfig.class)
@EnableConfigurationProperties(AcademicPolicy.class)
class SecurityAndErrorWebMvcTest {

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
    private TaiKhoanService taiKhoanService;

    @MockitoBean
    private VaiTroService vaiTroService;

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
    private AuditService auditService;

    @Test
    @DisplayName("GET /error/403: Trả về HTTP 403 và view error/403 thay vì 404")
    void accessDeniedRoute_returnsHttp403AndView() throws Exception {
        mockMvc.perform(get("/error/403"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("GET /admin/tai-khoan: Phòng đào tạo bị chặn (forward/redirect 403 -> /error/403)")
    void taiKhoanEndpoint_phongDaoTaoRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/tai-khoan"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/tai-khoan: ADMIN truy cập hợp lệ")
    void taiKhoanEndpoint_adminRole_ok() throws Exception {
        when(taiKhoanService.searchRows(anyString(), any(), any())).thenReturn(Page.empty());
        when(vaiTroService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/tai-khoan"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tai-khoan/list"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/tai-khoan/toggle-lock: Thiếu CSRF bị chặn 403")
    void toggleLock_missingCsrf_forbidden() throws Exception {
        mockMvc.perform(post("/admin/tai-khoan/toggle-lock").param("email", "sv@student.edu.vn"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/tai-khoan/toggle-lock: Có CSRF thành công và redirect PRG")
    void toggleLock_withCsrf_redirects() throws Exception {
        when(taiKhoanService.toggleLock(anyString(), any())).thenReturn(true);

        mockMvc.perform(post("/admin/tai-khoan/toggle-lock")
                        .with(csrf())
                        .param("email", "sv@student.edu.vn"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tai-khoan"));
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("POST /admin/lop-hoc-phan/{maLHP}/diem (GATE 3c): PHONG_DAO_TAO được phép lưu và redirect")
    void adminSaveGrades_phongDaoTaoRole_redirects() throws Exception {
        mockMvc.perform(post("/admin/lop-hoc-phan/INT2204_1/diem")
                        .with(csrf())
                        .param("lyDoChinhSua", "Phúc khảo kỳ 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/lop-hoc-phan/INT2204_1/diem"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/lop-hoc-phan/{maLHP}/diem (GATE 3c): ADMIN bị chặn 403 khi cố sửa điểm")
    void adminSaveGrades_adminRole_forbidden() throws Exception {
        mockMvc.perform(post("/admin/lop-hoc-phan/INT2204_1/diem")
                        .with(csrf())
                        .param("lyDoChinhSua", "Cố tình sửa"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/nhat-ky: ADMIN xem nhật ký hệ thống thành công")
    void auditLog_adminRole_ok() throws Exception {
        when(auditService.search(anyString(), any(), any(), any())).thenReturn(Page.empty());
        when(auditService.findDistinctDoiTuong()).thenReturn(List.of());
        when(auditService.findDistinctHanhDong()).thenReturn(List.of());

        mockMvc.perform(get("/admin/nhat-ky"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/nhat-ky/list"));
    }

    @Test
    @WithMockUser(roles = "PHONG_DAO_TAO")
    @DisplayName("GET /admin/nhat-ky: PHONG_DAO_TAO bị chặn 403")
    void auditLog_phongDaoTaoRole_forbidden() throws Exception {
        mockMvc.perform(get("/admin/nhat-ky"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }
}
