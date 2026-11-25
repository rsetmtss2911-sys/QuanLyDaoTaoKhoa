package com.qldtkhoa.qldtkhoa_app.controller.admin;

import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LichGiangDayService;
import com.qldtkhoa.qldtkhoa_app.service.LopHocPhanService;
import com.qldtkhoa.qldtkhoa_app.service.NhanSuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminXepLichControllerTest {

    @Mock
    private LichGiangDayService lichGiangDayService;
    @Mock
    private LopHocPhanService lopHocPhanService;
    @Mock
    private HocKyService hocKyService;
    @Mock
    private NhanSuService nhanSuService;

    @InjectMocks
    private AdminXepLichController controller;

    @Test
    @DisplayName("index should populate model and return admin/xep-lich/index")
    void index_validParams_returnsIndexView() {
        HocKyNamHoc hk = mock(HocKyNamHoc.class);
        when(hocKyService.findAll()).thenReturn(List.of(hk));
        when(lichGiangDayService.getDistinctRooms("HK1_2025")).thenReturn(List.of("301-G2"));
        when(nhanSuService.findAllGiangVien()).thenReturn(List.of());
        when(lichGiangDayService.getRoomTimetable("301-G2", "HK1_2025")).thenReturn(List.of());
        when(lichGiangDayService.getActiveSections("HK1_2025")).thenReturn(List.of());
        when(lichGiangDayService.getUnscheduledSections("HK1_2025")).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = controller.index("HK1_2025", "THEO_PHONG", "301-G2", null, model);

        assertThat(view).isEqualTo("admin/xep-lich/index");
        assertThat(model.getAttribute("currentHk")).isEqualTo("HK1_2025");
        assertThat(model.getAttribute("phongHoc")).isEqualTo("301-G2");
        assertThat(model.getAttribute("slots")).isNotNull();
    }

    @Test
    @DisplayName("saveSchedule should call lopHocPhanService.addSchedule and redirect")
    void saveSchedule_validInput_redirectsWithSuccess() {
        LichGiangDayForm form = new LichGiangDayForm();
        form.setThuTrongTuan(2);
        form.setTietBatDau(1);
        form.setTietKetThuc(3);
        form.setPhongHoc("301-G2");

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "scheduleForm");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.saveSchedule("LHP01", form, bindingResult, "HK1_2025", "THEO_PHONG", "301-G2", null, redirect);

        assertThat(view).contains("redirect:/admin/xep-lich");
        verify(lopHocPhanService, times(1)).addSchedule(eq("LHP01"), eq(form));
        assertThat(redirect.getFlashAttributes()).containsKey("flash");
    }

    @Test
    @DisplayName("deleteSchedule should call lopHocPhanService.removeSchedule and redirect")
    void deleteSchedule_validParams_redirectsWithSuccess() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.deleteSchedule("LHP01", 2, 1, "HK1_2025", "THEO_PHONG", "301-G2", null, redirect);

        assertThat(view).contains("redirect:/admin/xep-lich");
        verify(lopHocPhanService, times(1)).removeSchedule("LHP01", 2, 1);
        assertThat(redirect.getFlashAttributes()).containsKey("flash");
    }
}
