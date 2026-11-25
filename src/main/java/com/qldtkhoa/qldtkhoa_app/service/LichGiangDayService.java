package com.qldtkhoa.qldtkhoa_app.service;

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
        if (schedules == null || schedules.isEmpty()) {
            List<DangKyHocPhan> dangKys = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK);
            List<String> registeredLhpIds = dangKys.stream()
                    .filter(dk -> (dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                            && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY)
                    .map(dk -> dk.getLopHocPhan().getMaLHP())
                    .toList();
            if (!registeredLhpIds.isEmpty()) {
                schedules = lichGiangDayRepository.findByMaLHPIn(registeredLhpIds);
            } else {
                schedules = List.of();
            }
        }
        return schedules.stream().map(this::toSlot).toList();
    }

    public List<RegisteredCourseTimetableRow> getRegisteredCoursesSummary(String maSV, String maHK) {
        List<DangKyHocPhan> dangKys = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK);
        List<RegisteredCourseTimetableRow> result = new ArrayList<>();
        for (DangKyHocPhan dk : dangKys) {
            if ((dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                    && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY) {
                LopHocPhan lhp = dk.getLopHocPhan();
                List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(lhp.getMaLHP());
                String scheduleText;
                if (schedules.isEmpty()) {
                    scheduleText = "Chờ xếp lịch";
                } else {
                    scheduleText = schedules.stream()
                            .map(s -> (s.getThuTrongTuan() == 8 ? "Chủ nhật" : "Thứ " + s.getThuTrongTuan())
                                    + " (Tiết " + s.getTietBatDau() + "-" + s.getTietKetThuc() + ", P." + s.getPhongHoc() + ")")
                            .reduce((a, b) -> a + " • " + b)
                            .orElse("Chờ xếp lịch");
                }
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
        List<DangKyHocPhan> dangKys = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK);
        List<UnscheduledCourseRow> result = new ArrayList<>();
        for (DangKyHocPhan dk : dangKys) {
            if ((dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                    && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY) {
                LopHocPhan lhp = dk.getLopHocPhan();
                List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(lhp.getMaLHP());
                if (schedules.isEmpty()) {
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
        }
        return result;
    }

    public List<LopHocPhan> getUnscheduledSections(String maHK) {
        return lopHocPhanRepository.findUnscheduledSections(maHK);
    }

    public List<LopHocPhan> getActiveSections(String maHK) {
        return lopHocPhanRepository.findActiveSectionsByHocKy(maHK);
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
