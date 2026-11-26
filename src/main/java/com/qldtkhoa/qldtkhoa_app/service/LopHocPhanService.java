package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.LichGiangDayForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanForm;
import com.qldtkhoa.qldtkhoa_app.dto.LopHocPhanRow;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.LichGiangDayId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import com.qldtkhoa.qldtkhoa_app.util.LikeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LopHocPhanService {

    private static final List<TrangThaiDangKy> ACTIVE_REG_STATUSES =
            List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI);

    private final LopHocPhanRepository lopHocPhanRepository;
    private final LichGiangDayRepository lichGiangDayRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final KetQuaHocTapRepository ketQuaHocTapRepository;
    private final AcademicPolicy academicPolicy;
    private final AuditService auditService;

    public Page<LopHocPhanRow> search(String q, String maHK, String maMon, String maGV, TrangThaiLop status, Pageable pageable) {
        Page<LopHocPhan> page = lopHocPhanRepository.search(LikeUtils.escape(q), maHK, maMon, maGV, status, pageable);
        Map<String, Long> countMap = loadActiveCountMap(page.getContent());
        return page.map(lhp -> toRow(lhp, countMap.getOrDefault(lhp.getMaLHP(), 0L)));
    }

    public LopHocPhan getOrThrow(String maLHP) {
        return lopHocPhanRepository.findById(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));
    }

    public LopHocPhan getWithDetailsOrThrow(String maLHP) {
        return lopHocPhanRepository.findByIdWithDetails(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));
    }

    public LopHocPhanForm getForm(String maLHP) {
        LopHocPhan lhp = getOrThrow(maLHP);
        LopHocPhanForm form = new LopHocPhanForm();
        form.setMaLHP(lhp.getMaLHP());
        form.setMaMon(lhp.getMonHoc().getMaMon());
        form.setMaGV(lhp.getGiangVien() != null ? lhp.getGiangVien().getMaNS() : null);
        form.setMaHK(lhp.getHocKy().getMaHK());
        form.setSiSoToiDa(lhp.getSiSoToiDa());
        form.setTrangThaiLop(lhp.getTrangThaiLop());
        return form;
    }

    public List<LichGiangDay> findSchedules(String maLHP) {
        return lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
    }

    public List<DangKyHocPhan> findRegistrations(String maLHP) {
        return dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(maLHP);
    }

    public long countActiveRegistrations(String maLHP) {
        return dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(maLHP, ACTIVE_REG_STATUSES);
    }

    public List<LopHocPhanRow> findByLecturerAndTerm(String maNS, String maHK) {
        List<LopHocPhan> sections = (maHK != null && !maHK.isBlank() && !"ALL".equalsIgnoreCase(maHK))
                ? lopHocPhanRepository.findByHocKy_MaHKAndGiangVien_MaNS(maHK, maNS)
                : lopHocPhanRepository.findByGiangVien_MaNS(maNS);
        Map<String, Long> countMap = loadActiveCountMap(sections);
        return sections.stream()
                .map(lhp -> toRow(lhp, countMap.getOrDefault(lhp.getMaLHP(), 0L)))
                .toList();
    }

    @Transactional
    public void create(LopHocPhanForm form) {
        String ma = form.getMaLHP().trim();
        if (lopHocPhanRepository.existsById(ma)) {
            throw new DuplicateResourceException("lophp.maLHP.duplicate", ma);
        }

        MonHoc mon = monHocRepository.findById(form.getMaMon())
                .orElseThrow(() -> new ResourceNotFoundException("monhoc.notFound", form.getMaMon()));
        GiangVien gv = giangVienRepository.findById(form.getMaGV())
                .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", form.getMaGV()));
        HocKyNamHoc hk = hocKyNamHocRepository.findById(form.getMaHK())
                .orElseThrow(() -> new ResourceNotFoundException("hocky.notFound", form.getMaHK()));

        LopHocPhan lhp = new LopHocPhan(
                ma, mon, gv, hk,
                form.getSiSoToiDa(),
                TrangThaiLop.CHO_MO
        );
        lopHocPhanRepository.save(lhp);
    }

    @Transactional
    public void update(String maLHP, LopHocPhanForm form) {
        LopHocPhan lhp = getOrThrow(maLHP);

        long currentEnrolled = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, ACTIVE_REG_STATUSES);
        if (form.getSiSoToiDa() != null && form.getSiSoToiDa() < currentEnrolled) {
            throw new BusinessRuleException("lophp.capacity.belowEnrolled", currentEnrolled);
        }

        GiangVien newGv = giangVienRepository.findById(form.getMaGV())
                .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", form.getMaGV()));

        String oldMaGV = lhp.getGiangVien() != null ? lhp.getGiangVien().getMaNS() : null;
        if (lhp.getTrangThaiLop() != TrangThaiLop.DA_HUY
                && (oldMaGV == null || !oldMaGV.equalsIgnoreCase(newGv.getMaNS()))) {
            checkLecturerScheduleConflicts(maLHP, lhp.getHocKy().getMaHK(), newGv);
        }

        lhp.setGiangVien(newGv);
        lhp.setSiSoToiDa(form.getSiSoToiDa());
    }

    @Transactional
    public void lockEnrollment(String maLHP, String actor) {
        LopHocPhan lhp = lopHocPhanRepository.findByIdForUpdate(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (lhp.getTrangThaiLop() != TrangThaiLop.CHO_MO) {
            throw new BusinessRuleException("lophp.lock.invalidState", lhp.getTrangThaiLop().getLabel());
        }

        List<LichGiangDay> schedules = lichGiangDayRepository
                .findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        if (schedules.isEmpty()) {
            throw new BusinessRuleException("lophp.lock.noSchedule");
        }

        long currentEnrolled = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, ACTIVE_REG_STATUSES);
        int minStudents = academicPolicy.minSectionStudents();
        if (currentEnrolled < minStudents) {
            throw new BusinessRuleException("lophp.lock.belowMinStudents", currentEnrolled, minStudents);
        }

        lhp.setTrangThaiLop(TrangThaiLop.DA_CHOT_DANH_SACH);
        lopHocPhanRepository.saveAndFlush(lhp);
        auditService.logAs(actor, "LOCK_ENROLLMENT", "LopHocPhan", maLHP,
                TrangThaiLop.CHO_MO.getLabel(), TrangThaiLop.DA_CHOT_DANH_SACH.getLabel(),
                "Sĩ số chốt: " + currentEnrolled);
    }

    @Transactional
    public void lockGrades(String maLHP, String actor) {
        LopHocPhan lhp = lopHocPhanRepository.findByIdForUpdate(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (lhp.getTrangThaiLop() != TrangThaiLop.DA_CHOT_DANH_SACH) {
            throw new BusinessRuleException("lophp.lockGrades.invalidState", lhp.getTrangThaiLop().getLabel());
        }

        lhp.setTrangThaiLop(TrangThaiLop.DA_KHOA_DIEM);
        lopHocPhanRepository.saveAndFlush(lhp);
        auditService.logAs(actor, "LOCK_GRADES", "LopHocPhan", maLHP,
                TrangThaiLop.DA_CHOT_DANH_SACH.getLabel(), TrangThaiLop.DA_KHOA_DIEM.getLabel(),
                "Khóa bảng điểm lớp học phần");
    }

    @Transactional
    public int cancelSection(String maLHP, String lyDo, String actor) {
        if (lyDo == null || lyDo.isBlank()) {
            throw new BusinessRuleException("lophp.cancel.reasonRequired");
        }

        LopHocPhan lhp = lopHocPhanRepository.findByIdForUpdate(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (lhp.getTrangThaiLop() != TrangThaiLop.CHO_MO
                && lhp.getTrangThaiLop() != TrangThaiLop.DA_CHOT_DANH_SACH) {
            throw new BusinessRuleException("lophp.cancel.invalidState", lhp.getTrangThaiLop().getLabel());
        }

        if (ketQuaHocTapRepository.existsByDangKy_LopHocPhan_MaLHP(maLHP)) {
            throw new BusinessRuleException("lophp.cancel.hasGrades");
        }

        String oldStatus = lhp.getTrangThaiLop().getLabel();
        List<DangKyHocPhan> regs = dangKyHocPhanRepository.findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(maLHP);
        List<DangKyHocPhan> toWithdraw = new ArrayList<>();
        for (DangKyHocPhan dk : regs) {
            if (dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI) {
                dk.setTrangThaiDangKy(TrangThaiDangKy.DA_RUT_LOP);
                toWithdraw.add(dk);
            }
        }
        if (!toWithdraw.isEmpty()) {
            dangKyHocPhanRepository.saveAllAndFlush(toWithdraw);
        }

        lhp.setTrangThaiLop(TrangThaiLop.DA_HUY);
        lopHocPhanRepository.saveAndFlush(lhp);

        auditService.logAs(actor, "CANCEL_SECTION", "LopHocPhan", maLHP,
                oldStatus, TrangThaiLop.DA_HUY.getLabel(),
                lyDo.trim() + " (Rút " + toWithdraw.size() + " SV)");
        return toWithdraw.size();
    }

    @Transactional
    public void delete(String maLHP) {
        getOrThrow(maLHP);
        if (dangKyHocPhanRepository.existsByLopHocPhan_MaLHP(maLHP)) {
            throw new BusinessRuleException("lophp.delete.hasRegistrations", maLHP);
        }

        List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        lichGiangDayRepository.deleteAll(schedules);

        lopHocPhanRepository.deleteById(maLHP);
    }

    @Transactional
    public void addSchedule(String maLHP, LichGiangDayForm form) {
        LopHocPhan lhp = getOrThrow(maLHP);

        if (lhp.getTrangThaiLop() == TrangThaiLop.DA_HUY || lhp.getTrangThaiLop() == TrangThaiLop.DA_KHOA_DIEM) {
            throw new BusinessRuleException("lophp.schedule.lockedState", lhp.getTrangThaiLop().getLabel());
        }

        if (form.getTietBatDau() == null || form.getTietKetThuc() == null ||
                form.getTietBatDau() < 1 || form.getTietKetThuc() > 15 ||
                form.getTietBatDau() > form.getTietKetThuc()) {
            throw new BusinessRuleException("lophp.schedule.invalidPeriods");
        }

        if (form.getThuTrongTuan() == null || form.getThuTrongTuan() < 2 || form.getThuTrongTuan() > 8) {
            throw new BusinessRuleException("lophp.schedule.invalidDay");
        }

        int newThu = form.getThuTrongTuan();
        int newStart = form.getTietBatDau();
        int newEnd = form.getTietKetThuc();
        String newRoom = form.getPhongHoc().trim();
        String maHK = lhp.getHocKy().getMaHK();
        String maGV = lhp.getGiangVien() != null ? lhp.getGiangVien().getMaNS() : null;

        // 1. Kiểm tra trùng lịch ngay trong chính lớp này (50022)
        List<LichGiangDay> currentSchedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        for (LichGiangDay ex : currentSchedules) {
            if (ex.getThuTrongTuan().equals(newThu) && isOverlap(newStart, newEnd, ex.getTietBatDau(), ex.getTietKetThuc())) {
                throw new BusinessRuleException("lophp.schedule.selfConflict",
                        newThu, ex.getTietBatDau() + "-" + ex.getTietKetThuc());
            }
        }

        // 2. Kiểm tra Xung đột phòng học (Room Conflict - Mã lỗi 50020)
        List<LichGiangDay> roomSchedules = lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc(maHK, newRoom);
        for (LichGiangDay ex : roomSchedules) {
            if (!ex.getLopHocPhan().getMaLHP().equals(maLHP)
                    && ex.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY
                    && ex.getThuTrongTuan().equals(newThu)
                    && isOverlap(newStart, newEnd, ex.getTietBatDau(), ex.getTietKetThuc())) {
                throw new BusinessRuleException("lophp.schedule.roomConflict",
                        newRoom, ex.getLopHocPhan().getMaLHP(), newThu, ex.getTietBatDau() + "-" + ex.getTietKetThuc());
            }
        }

        // 3. Kiểm tra Xung đột lịch giảng viên (Lecturer Conflict - Mã lỗi 50021)
        if (maGV != null) {
            List<LichGiangDay> gvSchedules = lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS(maHK, maGV);
            String tenGV = (lhp.getGiangVien().getNhanSu() != null)
                    ? lhp.getGiangVien().getNhanSu().getHoTen()
                    : maGV;
            for (LichGiangDay ex : gvSchedules) {
                if (!ex.getLopHocPhan().getMaLHP().equals(maLHP)
                        && ex.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY
                        && ex.getThuTrongTuan().equals(newThu)
                        && isOverlap(newStart, newEnd, ex.getTietBatDau(), ex.getTietKetThuc())) {
                    throw new BusinessRuleException("lophp.schedule.lecturerConflict",
                            tenGV, ex.getLopHocPhan().getMaLHP(), newThu, ex.getTietBatDau() + "-" + ex.getTietKetThuc());
                }
            }
        }

        // 4. Kiểm tra xung đột lịch của các sinh viên đã đăng ký lớp này
        checkEnrolledStudentScheduleConflicts(maLHP, maHK, newThu, newStart, newEnd);

        LichGiangDay lgd = new LichGiangDay(lhp, newThu, newStart, newEnd, newRoom);
        lichGiangDayRepository.save(lgd);
    }

    @Transactional
    public void removeSchedule(String maLHP, Integer thuTrongTuan, Integer tietBatDau) {
        LopHocPhan lhp = getOrThrow(maLHP);
        if (lhp.getTrangThaiLop() != TrangThaiLop.CHO_MO) {
            throw new BusinessRuleException("lophp.schedule.lockedState", lhp.getTrangThaiLop().getLabel());
        }
        LichGiangDayId id = new LichGiangDayId(maLHP, thuTrongTuan, tietBatDau);
        if (lichGiangDayRepository.existsById(id)) {
            lichGiangDayRepository.deleteById(id);
        }
    }

    private void checkLecturerScheduleConflicts(String maLHP, String maHK, GiangVien newGv) {
        List<LichGiangDay> sectionSlots = lichGiangDayRepository
                .findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        if (sectionSlots.isEmpty()) {
            return;
        }
        List<LichGiangDay> gvSlots = lichGiangDayRepository
                .findByLopHocPhan_HocKy_MaHKAndLopHocPhan_GiangVien_MaNS(maHK, newGv.getMaNS());
        String tenGv = newGv.getNhanSu() != null ? newGv.getNhanSu().getHoTen() : newGv.getMaNS();

        for (LichGiangDay s : sectionSlots) {
            for (LichGiangDay other : gvSlots) {
                if (!other.getLopHocPhan().getMaLHP().equals(maLHP)
                        && other.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY
                        && other.getThuTrongTuan().equals(s.getThuTrongTuan())
                        && isOverlap(s.getTietBatDau(), s.getTietKetThuc(), other.getTietBatDau(), other.getTietKetThuc())) {
                    throw new BusinessRuleException("lophp.schedule.lecturerConflict",
                            tenGv, other.getLopHocPhan().getMaLHP(), s.getThuTrongTuan(),
                            other.getTietBatDau() + "-" + other.getTietKetThuc());
                }
            }
        }
    }

    private void checkEnrolledStudentScheduleConflicts(String maLHP, String maHK, int thu, int start, int end) {
        List<String> studentIds = dangKyHocPhanRepository
                .findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(maLHP).stream()
                .filter(dk -> dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                .map(dk -> dk.getSinhVien().getMaSV())
                .distinct()
                .toList();
        if (studentIds.isEmpty()) {
            return;
        }
        List<DangKyHocPhan> otherRegs = dangKyHocPhanRepository.findOtherActiveRegistrationsForStudents(
                studentIds, maHK, maLHP, ACTIVE_REG_STATUSES, TrangThaiLop.DA_HUY);
        if (otherRegs.isEmpty()) {
            return;
        }
        List<String> otherLhpIds = otherRegs.stream()
                .map(dk -> dk.getLopHocPhan().getMaLHP())
                .distinct()
                .toList();
        Map<String, List<LichGiangDay>> otherScheduleMap = lichGiangDayRepository.findByMaLHPIn(otherLhpIds).stream()
                .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));

        for (DangKyHocPhan reg : otherRegs) {
            List<LichGiangDay> slots = otherScheduleMap.getOrDefault(reg.getLopHocPhan().getMaLHP(), List.of());
            for (LichGiangDay slot : slots) {
                if (slot.getThuTrongTuan().equals(thu) && isOverlap(start, end, slot.getTietBatDau(), slot.getTietKetThuc())) {
                    throw new BusinessRuleException("lophp.schedule.studentConflict",
                            reg.getSinhVien().getMaSV(), reg.getLopHocPhan().getMaLHP(),
                            thu, slot.getTietBatDau() + "-" + slot.getTietKetThuc());
                }
            }
        }
    }

    private Map<String, Long> loadActiveCountMap(List<LopHocPhan> sections) {
        if (sections == null || sections.isEmpty()) {
            return Map.of();
        }
        List<String> ids = sections.stream().map(lhp -> lhp.getMaLHP()).toList();
        List<Object[]> rows = dangKyHocPhanRepository.countActiveByMaLHPIn(ids, ACTIVE_REG_STATUSES);
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private boolean isOverlap(int start1, int end1, int start2, int end2) {
        return Math.max(start1, start2) <= Math.min(end1, end2);
    }

    public LopHocPhanRow toRow(LopHocPhan lhp) {
        long currentEnrolled = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                lhp.getMaLHP(), ACTIVE_REG_STATUSES);
        return toRow(lhp, currentEnrolled);
    }

    private LopHocPhanRow toRow(LopHocPhan lhp, long currentEnrolled) {
        String maGV = lhp.getGiangVien() != null ? lhp.getGiangVien().getMaNS() : null;
        String tenGV = (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null)
                ? lhp.getGiangVien().getNhanSu().getHoTen()
                : "Chưa phân công";

        return new LopHocPhanRow(
                lhp.getMaLHP(),
                lhp.getMonHoc().getMaMon(),
                lhp.getMonHoc().getTenMon(),
                lhp.getMonHoc().getSoTinChi(),
                maGV,
                tenGV,
                lhp.getHocKy().getMaHK(),
                lhp.getSiSoToiDa(),
                currentEnrolled,
                lhp.getTrangThaiLop()
        );
    }
}
