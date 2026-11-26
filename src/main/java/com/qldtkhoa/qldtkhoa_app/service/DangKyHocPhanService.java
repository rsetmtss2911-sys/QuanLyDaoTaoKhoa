package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyDaChonRow;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyItemRow;
import com.qldtkhoa.qldtkhoa_app.dto.SinhVienDangKyRow;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DangKyHocPhanService {

    private static final List<TrangThaiDangKy> ACTIVE_REG_STATUSES = List.of(TrangThaiDangKy.HOP_LE,
            TrangThaiDangKy.CAM_THI);

    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DieuKienMonHocRepository dieuKienMonHocRepository;
    private final KetQuaHocTapRepository ketQuaHocTapRepository;
    private final LichGiangDayRepository lichGiangDayRepository;
    private final ThongKeLopHocPhanViewRepository thongKeLopHocPhanViewRepository;
    private final ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;
    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final AcademicPolicy academicPolicy;
    private final Clock clock;

    public List<DangKyItemRow> getAvailableSections(String maSV, String maHK) {
        return evaluateSectionEligibility(maSV, maHK);
    }

    /**
     * Đánh giá trước toàn bộ điều kiện đăng ký từng lớp học phần cho sinh viên
     * (không N+1),
     * trả sẵn lý do chặn và cảnh báo để hiển thị ngay trên giao diện đăng ký.
     */
    public List<DangKyItemRow> evaluateSectionEligibility(String maSV, String maHK) {
        if (maHK == null || maHK.isBlank()) {
            return List.of();
        }

        List<LopHocPhan> sections = lopHocPhanRepository.findByHocKy_MaHKAndTrangThaiLop(maHK, TrangThaiLop.CHO_MO);
        if (sections.isEmpty()) {
            return List.of();
        }

        SinhVien sv = sinhVienRepository.findByIdWithDetails(maSV).orElse(null);
        HocKyNamHoc term = hocKyNamHocRepository.findById(maHK).orElse(sections.get(0).getHocKy());
        LocalDate today = currentDate();
        boolean insideWindow = isInsideRegistrationWindow(term, today);

        List<DangKyHocPhan> allStudentTermRegs = dangKyHocPhanRepository
                .findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK);

        List<DangKyHocPhan> activeTermRegs = allStudentTermRegs.stream()
                .filter(this::isActiveRegistration)
                .toList();

        Map<String, TrangThaiDangKy> regStatusByLhp = allStudentTermRegs.stream()
                .collect(Collectors.toMap(dk -> dk.getLopHocPhan().getMaLHP(), dk -> dk.getTrangThaiDangKy(),
                        (a, b) -> a));

        Set<String> registeredLhpIds = activeTermRegs.stream()
                .map(dk -> dk.getLopHocPhan().getMaLHP())
                .collect(Collectors.toSet());
        Set<String> registeredSubjectIds = activeTermRegs.stream()
                .map(dk -> dk.getLopHocPhan().getMonHoc().getMaMon())
                .collect(Collectors.toSet());

        int currentCredits = activeTermRegs.stream()
                .mapToInt(dk -> dk.getLopHocPhan().getMonHoc().getSoTinChi())
                .sum();

        List<String> openLhpIds = sections.stream().map(lhp -> lhp.getMaLHP()).toList();
        List<String> allInvolvedLhpIds = new ArrayList<>(openLhpIds);
        for (String id : registeredLhpIds) {
            if (!allInvolvedLhpIds.contains(id)) {
                allInvolvedLhpIds.add(id);
            }
        }

        Map<String, List<LichGiangDay>> scheduleMap = allInvolvedLhpIds.isEmpty()
                ? Map.of()
                : lichGiangDayRepository.findByMaLHPIn(allInvolvedLhpIds).stream()
                        .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));

        Map<String, Long> enrolledCountMap = thongKeLopHocPhanViewRepository.findByMaHKOrderByTenMonAsc(maHK).stream()
                .collect(Collectors.toMap(
                        v -> v.getMaLHP(),
                        v -> v.getSiSoHienTai() != null ? v.getSiSoHienTai().longValue() : 0L,
                        (a, b) -> a));
        // Fallback nếu test mock không trả view rows
        if (enrolledCountMap.isEmpty() && !openLhpIds.isEmpty()) {
            enrolledCountMap = toCountMap(
                    dangKyHocPhanRepository.countActiveByMaLHPIn(openLhpIds, ACTIVE_REG_STATUSES));
        }

        List<String> maMons = sections.stream().map(l -> l.getMonHoc().getMaMon()).distinct().toList();
        Map<String, List<DieuKienMonHoc>> prereqsByMon = maMons.isEmpty()
                ? Map.of()
                : dieuKienMonHocRepository.findByMonHoc_MaMonIn(maMons).stream()
                        .collect(Collectors.groupingBy(d -> d.getMonHoc().getMaMon()));

        LocalDate termStart = term != null ? term.getNgayBatDau() : null;
        Set<String> passedBeforeTerm = new HashSet<>(ketQuaHocTapRepository.findPassedMaMonBeforeTerm(
                maSV, academicPolicy.passScore(), termStart, TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_HUY));

        Set<String> curriculumSubjects = resolveCurriculumSubjects(sv);

        Map<String, Long> finalEnrolledMap = enrolledCountMap;
        return sections.stream().map(lhp -> {
            String maLHP = lhp.getMaLHP();
            String maMon = lhp.getMonHoc().getMaMon();
            boolean daDangKy = registeredLhpIds.contains(maLHP);
            boolean daDangKyMonKhac = !daDangKy && registeredSubjectIds.contains(maMon);
            long currentEnrolled = finalEnrolledMap.getOrDefault(maLHP, 0L);

            List<LichGiangDay> lhpSchedules = scheduleMap.getOrDefault(maLHP, List.of());
            String scheduleStr = formatSchedules(lhpSchedules);

            String blockReason = null;
            if (sv != null && sv.getTrangThaiHienTai() != TrangThaiSinhVien.DANG_HOC) {
                blockReason = "Trạng thái học vụ hiện tại (" + sv.getTrangThaiHienTai().getLabel()
                        + ") không được phép đăng ký";
            } else if (!insideWindow) {
                blockReason = "Ngoài thời gian mở cổng đăng ký học phần";
            } else if (regStatusByLhp.get(maLHP) == TrangThaiDangKy.CAM_THI) {
                blockReason = "Đang bị Cấm thi ở lớp học phần này";
            } else if (daDangKy) {
                blockReason = null;
            } else if (daDangKyMonKhac) {
                blockReason = "Đã đăng ký lớp khác của môn này trong kỳ";
            } else {
                List<String> missingPrereqs = new ArrayList<>();
                for (DieuKienMonHoc dk : prereqsByMon.getOrDefault(maMon, List.of())) {
                    String maTq = dk.getMonTienQuyet().getMaMon();
                    if (!passedBeforeTerm.contains(maTq)) {
                        missingPrereqs.add(dk.getMonTienQuyet().getTenMon() + " (" + maTq + ")");
                    }
                }
                if (!missingPrereqs.isEmpty()) {
                    blockReason = "Thiếu tiên quyết: " + String.join(", ", missingPrereqs);
                } else {
                    String conflictDesc = findScheduleConflict(lhpSchedules, activeTermRegs, scheduleMap);
                    if (conflictDesc != null) {
                        blockReason = conflictDesc;
                    } else if (currentCredits + lhp.getMonHoc().getSoTinChi() > academicPolicy.maxCreditsPerTerm()) {
                        blockReason = "Vượt trần " + academicPolicy.maxCreditsPerTerm() + " tín chỉ/kỳ";
                    } else if (currentEnrolled >= lhp.getSiSoToiDa()) {
                        blockReason = "Lớp học phần đã hết chỗ";
                    }
                }
            }

            String warningNote = null;
            if (blockReason == null && !daDangKy) {
                if (!curriculumSubjects.isEmpty() && !curriculumSubjects.contains(maMon)) {
                    warningNote = "Môn ngoài khung CTĐT của lớp hành chính";
                } else if (passedBeforeTerm.contains(maMon)) {
                    warningNote = "Đăng ký học cải thiện điểm";
                }
            }

            boolean eligible = !daDangKy && !daDangKyMonKhac && blockReason == null;
            String tenGV = (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null)
                    ? lhp.getGiangVien().getNhanSu().getHoTen()
                    : "Chưa phân công";

            return new DangKyItemRow(
                    maLHP,
                    maMon,
                    lhp.getMonHoc().getTenMon(),
                    lhp.getMonHoc().getSoTinChi(),
                    tenGV,
                    currentEnrolled,
                    lhp.getSiSoToiDa(),
                    scheduleStr,
                    lhp.getTrangThaiLop(),
                    daDangKy,
                    daDangKyMonKhac,
                    eligible,
                    blockReason,
                    warningNote);
        }).toList();
    }

    public List<DangKyDaChonRow> getSelectedCourses(String maSV, String maHK) {
        List<DangKyHocPhan> list = dangKyHocPhanRepository
                .findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK).stream()
                .filter(this::isActiveRegistration)
                .toList();
        if (list.isEmpty()) {
            return List.of();
        }
        List<String> lhpIds = list.stream().map(dk -> dk.getLopHocPhan().getMaLHP()).distinct().toList();
        Map<String, List<LichGiangDay>> scheduleMap = lichGiangDayRepository.findByMaLHPIn(lhpIds).stream()
                .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));

        return list.stream()
                .map(dk -> {
                    LopHocPhan lhp = dk.getLopHocPhan();
                    List<LichGiangDay> schedules = scheduleMap.getOrDefault(lhp.getMaLHP(), List.of());
                    return new DangKyDaChonRow(
                            lhp.getMaLHP(),
                            lhp.getMonHoc().getMaMon(),
                            lhp.getMonHoc().getTenMon(),
                            lhp.getMonHoc().getSoTinChi(),
                            (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null)
                                    ? lhp.getGiangVien().getNhanSu().getHoTen()
                                    : "Chưa phân công",
                            formatSchedules(schedules),
                            dk.getNgayDangKy(),
                            dk.getTrangThaiDangKy(),
                            lhp.getTrangThaiLop());
                }).toList();
    }

    public List<SinhVienDangKyRow> findBySinhVien(String maSV) {
        return dangKyHocPhanRepository.findBySinhVien_MaSV(maSV).stream()
                .map(dk -> new SinhVienDangKyRow(
                        dk.getLopHocPhan().getMaLHP(),
                        dk.getLopHocPhan().getMonHoc().getTenMon(),
                        dk.getLopHocPhan().getMonHoc().getSoTinChi(),
                        dk.getLopHocPhan().getHocKy().getMaHK(),
                        dk.getNgayDangKy(),
                        dk.getTrangThaiDangKy()))
                .toList();
    }

    @Transactional
    public void register(String maSV, String maLHP) {
        // Khóa theo thứ tự nhất quán (SinhVien trước -> LopHocPhan sau) để chống
        // deadlock & tránh L1 cache cũ
        SinhVien sv = sinhVienRepository.findByIdForUpdate(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));
        if (sv.getTrangThaiHienTai() != TrangThaiSinhVien.DANG_HOC) {
            throw new BusinessRuleException("reg.statusNotAllowed", sv.getTrangThaiHienTai().getLabel());
        }

        LopHocPhan lockedLhp = lopHocPhanRepository.findByIdForUpdate(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (lockedLhp.getTrangThaiLop() != TrangThaiLop.CHO_MO) {
            throw new BusinessRuleException("reg.sectionNotOpen", lockedLhp.getTrangThaiLop().getLabel());
        }

        HocKyNamHoc hocKy = lockedLhp.getHocKy();
        if (!isInsideRegistrationWindow(hocKy, currentDate())) {
            throw new BusinessRuleException("reg.windowClosed", hocKy != null ? hocKy.getMaHK() : "");
        }

        String maHK = hocKy.getMaHK();
        String maMon = lockedLhp.getMonHoc().getMaMon();

        // Kiểm tra đã đăng ký hoặc đang bị Cấm thi ở lớp này chưa (chặn tự gỡ Cấm thi)
        Optional<DangKyHocPhan> existingOpt = dangKyHocPhanRepository.findById(new DangKyHocPhanId(maSV, maLHP));
        if (existingOpt.isPresent()) {
            TrangThaiDangKy st = existingOpt.get().getTrangThaiDangKy();
            if (st == TrangThaiDangKy.HOP_LE) {
                throw new BusinessRuleException("reg.duplicateSection", maLHP);
            }
            if (st == TrangThaiDangKy.CAM_THI) {
                throw new BusinessRuleException("reg.error.examBannedReRegister");
            }
        }

        // Kiểm tra cùng môn học không được đăng ký 2 lớp trong cùng học kỳ (tính cả Cấm
        // thi, loại lớp Đã hủy)
        boolean duplicateSubject = dangKyHocPhanRepository.existsActiveCourseInTerm(
                maSV, maMon, maHK, ACTIVE_REG_STATUSES, TrangThaiLop.DA_HUY);
        if (duplicateSubject) {
            throw new BusinessRuleException("reg.duplicateSubject", lockedLhp.getMonHoc().getTenMon());
        }

        // Kiểm tra môn tiên quyết phải đạt ở học kỳ trước đó (loại trừ lớp Đã hủy và Đã
        // rút lớp)
        List<DieuKienMonHoc> prereqs = dieuKienMonHocRepository.findByMonHoc_MaMon(maMon);
        List<String> missingPrereqs = new ArrayList<>();
        LocalDate currentTermStart = hocKy.getNgayBatDau();
        for (DieuKienMonHoc dk : prereqs) {
            String maTq = dk.getMonTienQuyet().getMaMon();
            boolean passed = ketQuaHocTapRepository.hasPassedSubjectBeforeTerm(
                    maSV, maTq, academicPolicy.passScore(), currentTermStart,
                    TrangThaiDangKy.HOP_LE, TrangThaiLop.DA_HUY);
            if (!passed) {
                missingPrereqs.add(dk.getMonTienQuyet().getTenMon() + " (" + maTq + ")");
            }
        }
        if (!missingPrereqs.isEmpty()) {
            throw new BusinessRuleException("reg.prerequisiteMissing", String.join(", ", missingPrereqs));
        }

        // Kiểm tra giới hạn số tín chỉ tối đa trong kỳ
        List<DangKyHocPhan> currentDks = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK)
                .stream()
                .filter(this::isActiveRegistration)
                .toList();

        int currentCredits = currentDks.stream().mapToInt(dk -> dk.getLopHocPhan().getMonHoc().getSoTinChi()).sum();
        int newCredits = currentCredits + lockedLhp.getMonHoc().getSoTinChi();
        if (newCredits > academicPolicy.maxCreditsPerTerm()) {
            throw new BusinessRuleException("reg.maxCreditsExceeded",
                    academicPolicy.maxCreditsPerTerm(), currentCredits, lockedLhp.getMonHoc().getSoTinChi());
        }

        // Kiểm tra trùng lịch học (batch load lịch)
        List<String> scheduleLhpIds = new ArrayList<>();
        scheduleLhpIds.add(maLHP);
        currentDks.forEach(dk -> scheduleLhpIds.add(dk.getLopHocPhan().getMaLHP()));
        Map<String, List<LichGiangDay>> scheduleMap = lichGiangDayRepository.findByMaLHPIn(scheduleLhpIds).stream()
                .collect(Collectors.groupingBy(lgd -> lgd.getLopHocPhan().getMaLHP()));
        List<LichGiangDay> newSchedules = scheduleMap.getOrDefault(maLHP, List.of());

        for (DangKyHocPhan curDk : currentDks) {
            List<LichGiangDay> curSchedules = scheduleMap.getOrDefault(curDk.getLopHocPhan().getMaLHP(), List.of());
            for (LichGiangDay sNew : newSchedules) {
                for (LichGiangDay sCur : curSchedules) {
                    if (sNew.getThuTrongTuan().equals(sCur.getThuTrongTuan())
                            && overlaps(sNew.getTietBatDau(), sNew.getTietKetThuc(), sCur.getTietBatDau(),
                                    sCur.getTietKetThuc())) {
                        throw new BusinessRuleException("reg.scheduleConflict",
                                sNew.getTietBatDau() + "-" + sNew.getTietKetThuc(),
                                sNew.getThuTrongTuan(),
                                curDk.getLopHocPhan().getMonHoc().getTenMon(),
                                curDk.getLopHocPhan().getMaLHP());
                    }
                }
            }
        }

        // Kiểm tra sĩ số lớp học phần sau khi đã giữ khóa dòng
        long currentCount = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, ACTIVE_REG_STATUSES);
        if (currentCount >= lockedLhp.getSiSoToiDa()) {
            throw new BusinessRuleException("reg.sectionFull", lockedLhp.getSiSoToiDa(), lockedLhp.getSiSoToiDa());
        }

        LocalDateTime now = currentDateTime();
        if (existingOpt.isPresent()) {
            DangKyHocPhan existing = existingOpt.get();
            existing.setTrangThaiDangKy(TrangThaiDangKy.HOP_LE);
            existing.setNgayDangKy(now);
            existing.setLyDoCamThi(null);
            dangKyHocPhanRepository.saveAndFlush(existing);
        } else {
            DangKyHocPhan dk = DangKyHocPhan.of(sv, lockedLhp);
            dk.setNgayDangKy(now);
            dangKyHocPhanRepository.saveAndFlush(dk);
        }
    }

    @Transactional
    public void withdraw(String maSV, String maLHP) {
        DangKyHocPhan dk = dangKyHocPhanRepository.findById(new DangKyHocPhanId(maSV, maLHP))
                .orElseThrow(() -> new ResourceNotFoundException("registration.notFound", maLHP));

        if (dk.getTrangThaiDangKy() != TrangThaiDangKy.HOP_LE) {
            throw new BusinessRuleException("reg.withdraw.invalidStatus");
        }

        SinhVien sv = dk.getSinhVien();
        if (sv != null && sv.getTrangThaiHienTai() != TrangThaiSinhVien.DANG_HOC) {
            throw new BusinessRuleException("reg.statusNotAllowed", sv.getTrangThaiHienTai().getLabel());
        }

        LopHocPhan lhp = dk.getLopHocPhan();
        if (lhp != null && (lhp.getTrangThaiLop() == TrangThaiLop.DA_CHOT_DANH_SACH
                || lhp.getTrangThaiLop() == TrangThaiLop.DA_KHOA_DIEM)) {
            throw new BusinessRuleException("reg.withdraw.locked", lhp.getMaLHP());
        }
        if (lhp != null && lhp.getTrangThaiLop() == TrangThaiLop.DA_HUY) {
            throw new BusinessRuleException("reg.withdraw.cancelled", lhp.getMaLHP());
        }

        if (lhp != null && !isInsideWithdrawWindow(lhp.getHocKy(), currentDate())) {
            throw new BusinessRuleException("reg.withdraw.deadlinePassed");
        }

        Optional<KetQuaHocTap> kqOpt = ketQuaHocTapRepository.findById_MaSVAndId_MaLHP(maSV, maLHP);
        if (kqOpt.isPresent()) {
            KetQuaHocTap kq = kqOpt.get();
            if (kq.getDiemQuaTrinh() != null || kq.getDiemGiuaKy() != null
                    || kq.getDiemCuoiKy() != null || kq.getDiemTong() != null) {
                throw new BusinessRuleException("reg.withdrawGraded");
            }
        }

        dk.setTrangThaiDangKy(TrangThaiDangKy.DA_RUT_LOP);
        dangKyHocPhanRepository.saveAndFlush(dk);
    }

    public boolean isInsideRegistrationWindow(HocKyNamHoc hk, LocalDate today) {
        if (hk == null) {
            return true;
        }
        LocalDate open = hk.getNgayMoDangKy();
        LocalDate close = hk.getNgayDongDangKy();
        if (open == null && close == null) {
            if (hk.getNgayBatDau() == null) {
                return true;
            }
            int windowDays = academicPolicy.registrationWindowDays();
            open = hk.getNgayBatDau().minusDays(windowDays);
            close = hk.getNgayBatDau().plusDays(windowDays);
        }
        if (open != null && today.isBefore(open)) {
            return false;
        }
        return close == null || !today.isAfter(close);
    }

    public boolean isInsideWithdrawWindow(HocKyNamHoc hk, LocalDate today) {
        if (hk == null) {
            return true;
        }
        LocalDate deadline = hk.getHanRutHocPhan();
        if (deadline == null && hk.getNgayBatDau() != null) {
            deadline = hk.getNgayBatDau().plusDays(academicPolicy.withdrawDeadlineDays());
        }
        return deadline == null || !today.isAfter(deadline);
    }

    private Set<String> resolveCurriculumSubjects(SinhVien sv) {
        if (sv == null || sv.getLopHanhChinh() == null
                || sv.getLopHanhChinh().getNganhHoc() == null
                || sv.getLopHanhChinh().getKhoaNhapHoc() == null) {
            return Set.of();
        }
        String maNganh = sv.getLopHanhChinh().getNganhHoc().getMaNganh();
        String maKhoa = sv.getLopHanhChinh().getKhoaNhapHoc().getMaKhoaHoc();
        return chuongTrinhDaoTaoRepository
                .findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc(maNganh, maKhoa)
                .stream()
                .map(c -> c.getMonHoc().getMaMon())
                .collect(Collectors.toSet());
    }

    private String findScheduleConflict(List<LichGiangDay> newSchedules,
            List<DangKyHocPhan> activeTermRegs,
            Map<String, List<LichGiangDay>> scheduleMap) {
        if (newSchedules.isEmpty() || activeTermRegs.isEmpty()) {
            return null;
        }
        for (DangKyHocPhan cur : activeTermRegs) {
            List<LichGiangDay> curSchedules = scheduleMap.getOrDefault(cur.getLopHocPhan().getMaLHP(), List.of());
            for (LichGiangDay sNew : newSchedules) {
                for (LichGiangDay sCur : curSchedules) {
                    if (sNew.getThuTrongTuan().equals(sCur.getThuTrongTuan())
                            && overlaps(sNew.getTietBatDau(), sNew.getTietKetThuc(), sCur.getTietBatDau(),
                                    sCur.getTietKetThuc())) {
                        return "Trùng lịch T" + sNew.getThuTrongTuan()
                                + " (" + sNew.getTietBatDau() + "-" + sNew.getTietKetThuc() + ") với "
                                + cur.getLopHocPhan().getMonHoc().getTenMon();
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Object[] r : rows) {
            map.put((String) r[0], ((Number) r[1]).longValue());
        }
        return map;
    }

    private boolean overlaps(int start1, int end1, int start2, int end2) {
        return start1 <= end2 && start2 <= end1;
    }

    private boolean isActiveRegistration(DangKyHocPhan dk) {
        return (dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE || dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)
                && dk.getLopHocPhan() != null
                && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY;
    }

    private LocalDate currentDate() {
        return clock != null ? LocalDate.now(clock) : LocalDate.now();
    }

    private LocalDateTime currentDateTime() {
        return clock != null ? LocalDateTime.now(clock) : LocalDateTime.now();
    }

    private String formatSchedules(List<LichGiangDay> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return "Chưa xếp lịch";
        }
        return schedules.stream()
                .map(s -> (s.getThuTrongTuan() == 8 ? "CN" : "T" + s.getThuTrongTuan())
                        + " (" + s.getTietBatDau() + "-" + s.getTietKetThuc() + ") " + s.getPhongHoc())
                .collect(Collectors.joining("; "));
    }
}
