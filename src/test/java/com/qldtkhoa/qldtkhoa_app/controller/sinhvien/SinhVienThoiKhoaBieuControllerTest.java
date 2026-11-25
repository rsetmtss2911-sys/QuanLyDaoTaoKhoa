package com.qldtkhoa.qldtkhoa_app.controller.sinhvien;

import com.qldtkhoa.qldtkhoa_app.dto.RegisteredCourseTimetableRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
//import com.qldtkhoa.qldtkhoa_app.dto.UnscheduledCourseRow;
import com.qldtkhoa.qldtkhoa_app.entity.HocKyNamHoc;
import com.qldtkhoa.qldtkhoa_app.security.AppUserPrincipal;
import com.qldtkhoa.qldtkhoa_app.service.HocKyService;
import com.qldtkhoa.qldtkhoa_app.service.LichGiangDayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinhVienThoiKhoaBieuControllerTest {

    @Mock
    private LichGiangDayService lichGiangDayService;

    @Mock
    private HocKyService hocKyService;

    @InjectMocks
    private SinhVienThoiKhoaBieuController controller;

    @Test
    @DisplayName("viewTimetable should aggregate multi-course multi-lecturer timetable slots and return view")
    void viewTimetable_multipleCoursesAndLecturers_returnsTimetableWithAllSlots() {
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaSV()).thenReturn("SV001");

        HocKyNamHoc currentHk = new HocKyNamHoc("HK1_2025", "Học kỳ 1", 2025, LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 1, 15));
        when(hocKyService.findAll()).thenReturn(List.of(currentHk));
        when(hocKyService.getCurrentTerm()).thenReturn(currentHk);

        // Giả lập 2 môn học do 2 giảng viên khác nhau phụ trách
        TimetableSlot slot1 = new TimetableSlot(2, 1, 3, "Lập trình hướng đối tượng", "INT2204_1", "P301-G2",
                "TS. Nguyễn Văn A");
        TimetableSlot slot2 = new TimetableSlot(4, 6, 8, "Cơ sở dữ liệu", "INT2207_1", "P402-G2", "ThS. Trần Thị B");
        when(lichGiangDayService.getStudentTimetable("SV001", "HK1_2025")).thenReturn(List.of(slot1, slot2));

        RegisteredCourseTimetableRow row1 = new RegisteredCourseTimetableRow(
                "INT2204_1", "INT2204", "Lập trình hướng đối tượng", 3, "TS. Nguyễn Văn A",
                "Thứ 2 (Tiết 1-3, P.P301-G2)", "Đã chốt danh sách");
        RegisteredCourseTimetableRow row2 = new RegisteredCourseTimetableRow(
                "INT2207_1", "INT2207", "Cơ sở dữ liệu", 3, "ThS. Trần Thị B", "Thứ 4 (Tiết 6-8, P.P402-G2)",
                "Đã chốt danh sách");
        when(lichGiangDayService.getRegisteredCoursesSummary("SV001", "HK1_2025")).thenReturn(List.of(row1, row2));
        when(lichGiangDayService.getUnscheduledRegisteredCourses("SV001", "HK1_2025")).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = controller.viewTimetable(null, principal, model);

        assertThat(view).isEqualTo("sinhvien/thoi-khoa-bieu");
        assertThat(model.getAttribute("currentHk")).isEqualTo("HK1_2025");

        @SuppressWarnings("unchecked")
        List<TimetableSlot> slots = (List<TimetableSlot>) model.getAttribute("slots");
        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).tenGiangVien()).isEqualTo("TS. Nguyễn Văn A");
        assertThat(slots.get(1).tenGiangVien()).isEqualTo("ThS. Trần Thị B");

        @SuppressWarnings("unchecked")
        List<RegisteredCourseTimetableRow> registered = (List<RegisteredCourseTimetableRow>) model
                .getAttribute("registeredCourses");
        assertThat(registered).hasSize(2);
        assertThat(model.getAttribute("totalRegisteredCredits")).isEqualTo(6);
    }

    @Test
    @DisplayName("viewTimetable with specific maHK should use that maHK")
    void viewTimetable_withSpecificTerm_loadsThatTermTimetable() {
        AppUserPrincipal principal = mock(AppUserPrincipal.class);
        when(principal.getMaSV()).thenReturn("SV001");

        HocKyNamHoc hk = new HocKyNamHoc("HK2_2024", "Học kỳ 2", 2024, LocalDate.of(2025, 2, 3),
                LocalDate.of(2025, 6, 20));
        when(hocKyService.findAll()).thenReturn(List.of(hk));

        when(lichGiangDayService.getStudentTimetable("SV001", "HK2_2024")).thenReturn(List.of());
        when(lichGiangDayService.getRegisteredCoursesSummary("SV001", "HK2_2024")).thenReturn(List.of());
        when(lichGiangDayService.getUnscheduledRegisteredCourses("SV001", "HK2_2024")).thenReturn(List.of());

        Model model = new ConcurrentModel();
        String view = controller.viewTimetable("HK2_2024", principal, model);

        assertThat(view).isEqualTo("sinhvien/thoi-khoa-bieu");
        assertThat(model.getAttribute("currentHk")).isEqualTo("HK2_2024");
        assertThat(model.getAttribute("totalRegisteredCredits")).isEqualTo(0);
    }
}
