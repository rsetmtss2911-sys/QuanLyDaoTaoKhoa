package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.RegisteredCourseTimetableRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.dto.UnscheduledCourseRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.GiangVien;
import com.qldtkhoa.qldtkhoa_app.entity.LichGiangDay;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NhanSu;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LichGiangDayRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LichGiangDayServiceTest {

    @Mock
    private LichGiangDayRepository lichGiangDayRepository;
    @Mock
    private DangKyHocPhanRepository dangKyHocPhanRepository;
    @Mock
    private LopHocPhanRepository lopHocPhanRepository;

    @InjectMocks
    private LichGiangDayService service;

    @Test
    @DisplayName("getStudentTimetable & getLecturerTimetable ánh xạ sang TimetableSlot với fallback chưa phân công")
    void timetables_mapToTimetableSlot() {
        MonHoc mh = mock(MonHoc.class);
        when(mh.getTenMon()).thenReturn("Cơ sở dữ liệu");
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getMaLHP()).thenReturn("CSDL_1");
        when(lhp.getMonHoc()).thenReturn(mh);
        when(lhp.getGiangVien()).thenReturn(null);

        LichGiangDay s = mock(LichGiangDay.class);
        when(s.getThuTrongTuan()).thenReturn(2);
        when(s.getTietBatDau()).thenReturn(1);
        when(s.getTietKetThuc()).thenReturn(3);
        when(s.getPhongHoc()).thenReturn("301-G2");
        when(s.getLopHocPhan()).thenReturn(lhp);

        when(lichGiangDayRepository.findStudentWeeklyTimetable("SV01", "HK1")).thenReturn(List.of(s));
        when(lichGiangDayRepository.findLecturerWeeklyTimetable("GV01", "HK1")).thenReturn(List.of(s));

        List<TimetableSlot> svSlots = service.getStudentTimetable("SV01", "HK1");
        List<TimetableSlot> gvSlots = service.getLecturerTimetable("GV01", "HK1");

        assertThat(svSlots).hasSize(1);
        assertThat(svSlots.get(0).tenGiangVien()).isEqualTo("Chưa phân công");
        assertThat(gvSlots).hasSize(1);
        assertThat(gvSlots.get(0).phongHoc()).isEqualTo("301-G2");
    }

    @Test
    @DisplayName("getRegisteredCoursesSummary & getUnscheduledRegisteredCourses lọc bỏ đã rút lớp / lớp hủy")
    void registeredSummaryAndUnscheduled_filterActiveAndFormat() {
        MonHoc mh1 = mock(MonHoc.class);
        when(mh1.getMaMon()).thenReturn("INT2204");
        when(mh1.getTenMon()).thenReturn("OOP");
        when(mh1.getSoTinChi()).thenReturn(3);

        NhanSu ns = mock(NhanSu.class);
        when(ns.getHoTen()).thenReturn("TS. A");
        GiangVien gv = mock(GiangVien.class);
        when(gv.getNhanSu()).thenReturn(ns);

        LopHocPhan activeLhp = mock(LopHocPhan.class);
        when(activeLhp.getMaLHP()).thenReturn("OOP_1");
        when(activeLhp.getMonHoc()).thenReturn(mh1);
        when(activeLhp.getGiangVien()).thenReturn(gv);
        when(activeLhp.getTrangThaiLop()).thenReturn(TrangThaiLop.DA_CHOT_DANH_SACH);

        DangKyHocPhan activeDk = mock(DangKyHocPhan.class);
        when(activeDk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.HOP_LE);
        when(activeDk.getLopHocPhan()).thenReturn(activeLhp);

        DangKyHocPhan withdrawnDk = mock(DangKyHocPhan.class);
        when(withdrawnDk.getTrangThaiDangKy()).thenReturn(TrangThaiDangKy.DA_RUT_LOP);

        when(dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK("SV01", "HK1"))
                .thenReturn(List.of(activeDk, withdrawnDk));
        when(lichGiangDayRepository.findByMaLHPIn(List.of("OOP_1"))).thenReturn(List.of());

        List<RegisteredCourseTimetableRow> summary = service.getRegisteredCoursesSummary("SV01", "HK1");
        List<UnscheduledCourseRow> unscheduled = service.getUnscheduledRegisteredCourses("SV01", "HK1");

        assertThat(summary).hasSize(1);
        assertThat(summary.get(0).lichHoc()).isEqualTo("Chờ xếp lịch");
        assertThat(unscheduled).hasSize(1);
        assertThat(unscheduled.get(0).maLHP()).isEqualTo("OOP_1");
    }

    @Test
    @DisplayName("getActiveSectionOptions & getUnscheduledSectionOptions ánh xạ sang LopHocPhanOptionRow")
    void sectionOptions_mapToDto() {
        MonHoc mh = mock(MonHoc.class);
        when(mh.getMaMon()).thenReturn("INT1001");
        when(mh.getTenMon()).thenReturn("Tin học");
        when(mh.getSoTinChi()).thenReturn(2);
        LopHocPhan lhp = mock(LopHocPhan.class);
        when(lhp.getMaLHP()).thenReturn("TH_1");
        when(lhp.getMonHoc()).thenReturn(mh);

        when(lopHocPhanRepository.findActiveSectionsByHocKy("HK1")).thenReturn(List.of(lhp));
        when(lopHocPhanRepository.findUnscheduledSections("HK1")).thenReturn(List.of(lhp));

        List<LopHocPhanOptionRow> active = service.getActiveSectionOptions("HK1");
        List<LopHocPhanOptionRow> unscheduled = service.getUnscheduledSectionOptions("HK1");

        assertThat(active).containsExactly(new LopHocPhanOptionRow("TH_1", "INT1001", "Tin học", 2));
        assertThat(unscheduled).containsExactly(new LopHocPhanOptionRow("TH_1", "INT1001", "Tin học", 2));
    }
}
