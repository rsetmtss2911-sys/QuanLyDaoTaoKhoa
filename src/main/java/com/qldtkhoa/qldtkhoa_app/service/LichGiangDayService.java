package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanOptionRow;
import com.qldtkhoa.qldtkhoa_app.dto.RegisteredCourseTimetableRow;
import com.qldtkhoa.qldtkhoa_app.dto.TimetableSlot;
import com.qldtkhoa.qldtkhoa_app.dto.UnscheduledCourseRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.LichGiangDay;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LichGiangDayRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LichGiangDayService {

    private final LichGiangDayRepository lichGiangDayRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final LopHocPhanRepository lopHocPhanRepository;

    public LichGiangDayService(LichGiangDayRepository lichGiangDayRepository,
                               DangKyHocPhanRepository dangKyHocPhanRepository,
                               LopHocPhanRepository lopHocPhanRepository) {
        this.lichGiangDayRepository = lichGiangDayRepository;
        this.dangKyHocPhanRepository = dangKyHocPhanRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
    }

    public List<TimetableSlot> getLecturerTimetable(String maNS, String maHK) {
        List<LichGiangDay> schedules = lichGiangDayRepository.findLecturerWeeklyTimetable(maNS, maHK);
        return schedules.stream().map(this::toSlot).toList();
    }

    public List<TimetableSlot> getStudentTimetable(String maSV, String maHK) {
        List<LichGiangDay> schedules = lichGiangDayRepository.findStudentWeeklyTimetable(maSV, maHK);
        if (schedules == null) {
            schedules = List.of();
        }
        return schedules.stream().map(this::toSlot).toList();
    }

    public List<RegisteredCourseTimetableRow> getRegisteredCoursesSummary(String maSV, String maHK) {
        List<DangKyHocPhan> activeRegs = dangKyHocPhanRepository
                .findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK).stream()
                .filter(this::isActiveRegistration)
                .toList();
        if (activeRegs.isEmpty()) {
            return List.of();
        }
        List<String> lhpIds = activeRegs.stream().map(dk -> dk.getLopHocPhan().getMaLHP()).distinct().toList();
        Map<String, List<LichGiangDay>> scheduleMap = lichGiangDayRepository.findByMaLHPIn(lhpIds).stream()
                .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));

        List<RegisteredCourseTimetableRow> result = new ArrayList<>();
        for (DangKyHocPhan dk : activeRegs) {
            LopHocPhan lhp = dk.getLopHocPhan();
            List<LichGiangDay> schedules = scheduleMap.getOrDefault(lhp.getMaLHP(), List.of());
            String scheduleText = schedules.isEmpty()
                    ? "Chờ xếp lịch"
                    : schedules.stream()
                        .map(s -> (s.getThuTrongTuan() == 8 ? "Chủ nhật" : "Thứ " + s.getThuTrongTuan())
                                + " (Tiết " + s.getTietBatDau() + "-" + s.getTietKetThuc() + ", P." + s.getPhongHoc() + ")")
                        .collect(Collectors.joining(" • "));
            String tenGV = (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null)
                    ? lhp.getGiangVien().getNhanSu().getHoTen()
                    : "Chưa phân công";
            result.add(new RegisteredCourseTimetableRow(
                    lhp.getMaLHP(),
                    lhp.getMonHoc().getMaMon(),
                    lhp.getMonHoc().getTenMon(),
                    lhp.getMonHoc().getSoTinChi(),
                    tenGV,
                    scheduleText,
                    lhp.getTrangThaiLop().getLabel()
            ));
        }
        return result;
    }

    public List<TimetableSlot> getRoomTimetable(String phongHoc, String maHK) {
        List<LichGiangDay> schedules = lichGiangDayRepository.findRoomWeeklyTimetable(phongHoc, maHK);
        return schedules.stream().map(this::toSlot).toList();
    }

    public List<String> getDistinctRooms(String maHK) {
        return lichGiangDayRepository.findDistinctPhongHocByHocKy(maHK);
    }

    public List<UnscheduledCourseRow> getUnscheduledRegisteredCourses(String maSV, String maHK) {
        List<DangKyHocPhan> activeRegs = dangKyHocPhanRepository
                .findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK).stream()
                .filter(this::isActiveRegistration)
                .toList();
        if (activeRegs.isEmpty()) {
            return List.of();
        }
        List<String> lhpIds = activeRegs.stream().map(dk -> dk.getLopHocPhan().getMaLHP()).distinct().toList();
        Map<String, List<LichGiangDay>> scheduleMap = lichGiangDayRepository.findByMaLHPIn(lhpIds).stream()
                .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));

        List<UnscheduledCourseRow> result = new ArrayList<>();
        for (DangKyHocPhan dk : activeRegs) {
            LopHocPhan lhp = dk.getLopHocPhan();
            if (scheduleMap.getOrDefault(lhp.getMaLHP(), List.of()).isEmpty()) {
                String tenGV = (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null)
                        ? lhp.getGiangVien().getNhanSu().getHoTen()
                        : "Chưa phân công";
                result.add(new UnscheduledCourseRow(
                        lhp.getMaLHP(),
                        lhp.getMonHoc().getMaMon(),
                        lhp.getMonHoc().getTenMon(),
                        lhp.getMonHoc().getSoTinChi(),
                        tenGV,
                        lhp.getTrangThaiLop().getLabel()
                ));
            }
        }
        return result;
    }

    public List<LopHocPhan> getUnscheduledSections(String maHK) {
        return lopHocPhanRepository.findUnscheduledSections(maHK);
    }

    public List<LopHocPhanOptionRow> getUnscheduledSectionOptions(String maHK) {
        return getUnscheduledSections(maHK).stream().map(this::toOptionRow).toList();
    }

    public List<LopHocPhan> getActiveSections(String maHK) {
        return lopHocPhanRepository.findActiveSectionsByHocKy(maHK);
    }

    public List<LopHocPhanOptionRow> getActiveSectionOptions(String maHK) {
        return getActiveSections(maHK).stream().map(this::toOptionRow).toList();
    }

    private LopHocPhanOptionRow toOptionRow(LopHocPhan lhp) {
        return new LopHocPhanOptionRow(
                lhp.getMaLHP(),
                lhp.getMonHoc() != null ? lhp.getMonHoc().getMaMon() : "",
                lhp.getMonHoc() != null ? lhp.getMonHoc().getTenMon() : "",
                lhp.getMonHoc() != null ? lhp.getMonHoc().getSoTinChi() : null
        );
    }

    private boolean isActiveRegistration(DangKyHocPhan dk) {
        return (dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                && dk.getLopHocPhan() != null
                && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY;
    }

    private TimetableSlot toSlot(LichGiangDay s) {
        return new TimetableSlot(
                s.getThuTrongTuan(),
                s.getTietBatDau(),
                s.getTietKetThuc(),
                s.getLopHocPhan().getMonHoc().getTenMon(),
                s.getLopHocPhan().getMaLHP(),
                s.getPhongHoc(),
                (s.getLopHocPhan().getGiangVien() != null && s.getLopHocPhan().getGiangVien().getNhanSu() != null)
                        ? s.getLopHocPhan().getGiangVien().getNhanSu().getHoTen()
                        : "Chưa phân công"
        );
    }
}
