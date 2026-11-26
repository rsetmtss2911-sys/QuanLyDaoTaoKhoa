package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.*;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.entity.view.ThongKeLopHocPhanView;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final LopHocPhanService lopHocPhanService;
    private final MonHocRepository monHocRepository;
    private final LopHanhChinhRepository lopHanhChinhRepository;
    private final LopHanhChinhService lopHanhChinhService;
    private final QuyetDinhHocVuRepository quyetDinhHocVuRepository;
    private final ThongKeLopHocPhanViewRepository thongKeLopHocPhanViewRepository;
    private final HocKyService hocKyService;
    private final DangKyHocPhanService dangKyHocPhanService;
    private final LichGiangDayRepository lichGiangDayRepository;
    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
    private final AcademicPolicy academicPolicy;
    private final HocLucService hocLucService;

    public DashboardService(
            SinhVienRepository sinhVienRepository,
            GiangVienRepository giangVienRepository,
            LopHocPhanRepository lopHocPhanRepository,
            LopHocPhanService lopHocPhanService,
            MonHocRepository monHocRepository,
            LopHanhChinhRepository lopHanhChinhRepository,
            LopHanhChinhService lopHanhChinhService,
            QuyetDinhHocVuRepository quyetDinhHocVuRepository,
            ThongKeLopHocPhanViewRepository thongKeLopHocPhanViewRepository,
            HocKyService hocKyService,
            DangKyHocPhanService dangKyHocPhanService,
            LichGiangDayRepository lichGiangDayRepository,
            BangDiemSinhVienViewRepository bangDiemViewRepository,
            AcademicPolicy academicPolicy,
            HocLucService hocLucService) {
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienRepository = giangVienRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
        this.lopHocPhanService = lopHocPhanService;
        this.monHocRepository = monHocRepository;
        this.lopHanhChinhRepository = lopHanhChinhRepository;
        this.lopHanhChinhService = lopHanhChinhService;
        this.quyetDinhHocVuRepository = quyetDinhHocVuRepository;
        this.thongKeLopHocPhanViewRepository = thongKeLopHocPhanViewRepository;
        this.hocKyService = hocKyService;
        this.dangKyHocPhanService = dangKyHocPhanService;
        this.lichGiangDayRepository = lichGiangDayRepository;
        this.bangDiemViewRepository = bangDiemViewRepository;
        this.academicPolicy = academicPolicy;
        this.hocLucService = hocLucService;
    }

    public AdminDashboardView getAdminDashboard(String maHK) {
        List<HocKyNamHoc> hocKys = hocKyService.findAll();
        HocKyNamHoc currentTerm = null;
        if (maHK != null && !maHK.isBlank()) {
            currentTerm = hocKyService.getOrThrow(maHK);
        } else {
            currentTerm = hocKyService.getCurrentTerm();
            if (currentTerm == null && !hocKys.isEmpty()) {
                currentTerm = hocKys.get(0);
            }
        }
        String termCode = currentTerm != null ? currentTerm.getMaHK() : "";
        String termName = currentTerm != null ? currentTerm.getTenHK() + " (" + currentTerm.getNamHoc() + ")" : "";

        long totalStudentsActive = sinhVienRepository.countByTrangThaiHienTai(TrangThaiSinhVien.DANG_HOC);
        long totalStudentsTotal = sinhVienRepository.count();
        long totalLecturers = giangVienRepository.count();
        long totalCourses = monHocRepository.count();
        long totalAdminClasses = lopHanhChinhRepository.count();

        List<ThongKeLopHocPhanView> termViewList = termCode.isEmpty()
                ? List.of()
                : thongKeLopHocPhanViewRepository.findByMaHKOrderByTenMonAsc(termCode);

        long totalSectionsCurrentTerm = 0;
        long totalOpenSections = 0;
        if (!termCode.isEmpty()) {
            totalSectionsCurrentTerm = termViewList.isEmpty()
                    ? lopHocPhanRepository.countByHocKy_MaHK(termCode)
                    : termViewList.size();
            totalOpenSections = termViewList.stream()
                    .filter(v -> v.getTrangThaiLop() == TrangThaiLop.CHO_MO)
                    .count();
        }

        List<LopHocPhanRow> pendingSections = termViewList.stream()
                .filter(v -> v.getTrangThaiLop() == TrangThaiLop.CHO_MO)
                .limit(8)
                .map(v -> new LopHocPhanRow(
                        v.getMaLHP(),
                        v.getMaMon(),
                        v.getTenMon(),
                        v.getSoTinChi(),
                        v.getMaGV(),
                        v.getTenGiangVien() != null ? v.getTenGiangVien() : "Chưa phân công",
                        v.getMaHK(),
                        v.getSiSoToiDa(),
                        v.getSiSoHienTai() != null ? v.getSiSoHienTai().longValue() : 0L,
                        v.getTrangThaiLop()
                ))
                .toList();

        List<QuyetDinhRow> recentDecisions = quyetDinhHocVuRepository.findTop5ByOrderByNgayHieuLucDesc().stream()
                .map(qd -> new QuyetDinhRow(
                        qd.getMaQuyetDinh(),
                        qd.getSinhVien().getMaSV(),
                        qd.getSinhVien().getHoTen(),
                        qd.getSinhVien().getLopHanhChinh() != null ? qd.getSinhVien().getLopHanhChinh().getMaLopHC() : "—",
                        qd.getLoaiQuyetDinh(),
                        qd.getNgayHieuLuc(),
                        qd.getLyDo()
                )).toList();

        List<ThongKeLopHocPhanView> crowded = termViewList.stream()
                .filter(v -> v.getTrangThaiLop() == TrangThaiLop.CHO_MO
                        && v.getSoChoConTrong() != null
                        && v.getSoChoConTrong() <= 5)
                .limit(6)
                .toList();

        return new AdminDashboardView(
                totalStudentsActive,
                totalStudentsTotal,
                totalLecturers,
                totalSectionsCurrentTerm,
                totalOpenSections,
                totalCourses,
                totalAdminClasses,
                termCode,
                termName,
                hocKys,
                pendingSections,
                recentDecisions,
                crowded
        );
    }

    public GiangVienDashboardView getGiangVienDashboard(String maNS, String maHK) {
        GiangVien gv = giangVienRepository.findByIdWithDetails(maNS)
                .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", maNS));

        List<HocKyNamHoc> hocKys = hocKyService.findAll();
        HocKyNamHoc currentTerm = null;
        if (maHK != null && !maHK.isBlank()) {
            currentTerm = hocKyService.getOrThrow(maHK);
        } else {
            currentTerm = hocKyService.getCurrentTerm();
            if (currentTerm == null && !hocKys.isEmpty()) {
                currentTerm = hocKys.get(0);
            }
        }
        String termCode = currentTerm != null ? currentTerm.getMaHK() : "";
        String termName = currentTerm != null ? currentTerm.getTenHK() + " (" + currentTerm.getNamHoc() + ")" : "";

        List<LopHocPhanRow> sectionRows = termCode.isEmpty()
                ? List.of()
                : lopHocPhanService.findByLecturerAndTerm(maNS, termCode).stream()
                        .filter(r -> r.trangThaiLop() != TrangThaiLop.DA_HUY)
                        .toList();

        long totalAssignedSections = sectionRows.size();
        long totalStudentsTeaching = sectionRows.stream().mapToLong(r -> r.siSoHienTai()).sum();

        List<LichGiangDay> schedules = termCode.isEmpty()
                ? List.of()
                : lichGiangDayRepository.findLecturerWeeklyTimetable(maNS, termCode);

        List<TimetableSlot> weeklySchedule = schedules.stream()
                .map(s -> new TimetableSlot(
                        s.getThuTrongTuan(),
                        s.getTietBatDau(),
                        s.getTietKetThuc(),
                        s.getLopHocPhan().getMonHoc().getTenMon(),
                        s.getLopHocPhan().getMaLHP(),
                        s.getPhongHoc(),
                        gv.getNhanSu().getHoTen()
                )).toList();

        long totalTeachingHours = weeklySchedule.stream()
                .mapToLong(s -> (s.tietKetThuc() - s.tietBatDau() + 1))
                .sum();

        List<LopHanhChinhRow> advisedClasses = lopHanhChinhService.findByCoVan(maNS);
        long totalAdvised = advisedClasses.size();

        return new GiangVienDashboardView(
                gv.getMaNS(),
                gv.getNhanSu().getHoTen(),
                gv.getHocVi(),
                gv.getBoMon() != null ? gv.getBoMon().getTenBoMon() : "—",
                termCode,
                termName,
                hocKys,
                totalAssignedSections,
                totalStudentsTeaching,
                totalTeachingHours,
                totalAdvised,
                sectionRows,
                weeklySchedule,
                advisedClasses
        );
    }

    public SinhVienDashboardView getSinhVienDashboard(String maSV, String maHK) {
        SinhVien sv = sinhVienRepository.findByIdWithDetails(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));

        List<HocKyNamHoc> hocKys = hocKyService.findAll();
        HocKyNamHoc currentTerm = null;
        if (maHK != null && !maHK.isBlank()) {
            currentTerm = hocKyService.getOrThrow(maHK);
        } else {
            currentTerm = hocKyService.getCurrentTerm();
            if (currentTerm == null && !hocKys.isEmpty()) {
                currentTerm = hocKys.get(0);
            }
        }
        String termCode = currentTerm != null ? currentTerm.getMaHK() : "";
        String termName = currentTerm != null ? currentTerm.getTenHK() + " (" + currentTerm.getNamHoc() + ")" : "";

        List<DangKyDaChonRow> currentRegistrations = termCode.isEmpty()
                ? List.of()
                : dangKyHocPhanService.getSelectedCourses(maSV, termCode);

        int registeredCredits = currentRegistrations.stream()
                .mapToInt(r -> r.soTinChi() != null ? r.soTinChi() : 0)
                .sum();

        List<BangDiemSinhVienView> transcript = bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(maSV);
        HocLucSummary summary = hocLucService.evaluateSummary(transcript);
        int totalCumulativeCredits = summary.tongTCTichLuy();
        Double cumulativeGpa = summary.cpa4();
        String academicStanding = summary.xepLoai();

        List<LichGiangDay> schedules = termCode.isEmpty()
                ? List.of()
                : lichGiangDayRepository.findStudentWeeklyTimetable(maSV, termCode);

        List<TimetableSlot> weeklySchedule = schedules.stream()
                .map(s -> new TimetableSlot(
                        s.getThuTrongTuan(),
                        s.getTietBatDau(),
                        s.getTietKetThuc(),
                        s.getLopHocPhan().getMonHoc().getTenMon(),
                        s.getLopHocPhan().getMaLHP(),
                        s.getPhongHoc(),
                        (s.getLopHocPhan().getGiangVien() != null && s.getLopHocPhan().getGiangVien().getNhanSu() != null)
                                ? s.getLopHocPhan().getGiangVien().getNhanSu().getHoTen()
                                : "Chưa phân công"
                )).toList();

        List<QuyetDinhRow> recentDecisions = quyetDinhHocVuRepository.findBySinhVien_MaSVOrderByNgayHieuLucDesc(maSV).stream()
                .limit(5)
                .map(qd -> new QuyetDinhRow(
                        qd.getMaQuyetDinh(),
                        qd.getSinhVien().getMaSV(),
                        qd.getSinhVien().getHoTen(),
                        qd.getSinhVien().getLopHanhChinh() != null ? qd.getSinhVien().getLopHanhChinh().getMaLopHC() : "—",
                        qd.getLoaiQuyetDinh(),
                        qd.getNgayHieuLuc(),
                        qd.getLyDo()
                )).toList();

        String tenLopHC = sv.getLopHanhChinh() != null ? sv.getLopHanhChinh().getTenLopHC() : "—";
        String maLopHC = sv.getLopHanhChinh() != null ? sv.getLopHanhChinh().getMaLopHC() : "—";
        String tenNganh = (sv.getLopHanhChinh() != null && sv.getLopHanhChinh().getNganhHoc() != null)
                ? sv.getLopHanhChinh().getNganhHoc().getTenNganh() : "—";
        String tenKhoaHoc = (sv.getLopHanhChinh() != null && sv.getLopHanhChinh().getKhoaNhapHoc() != null)
                ? sv.getLopHanhChinh().getKhoaNhapHoc().getMaKhoaHoc() : "—";
        String tenCoVan = (sv.getLopHanhChinh() != null && sv.getLopHanhChinh().getCoVan() != null && sv.getLopHanhChinh().getCoVan().getNhanSu() != null)
                ? sv.getLopHanhChinh().getCoVan().getNhanSu().getHoTen() : "—";
        String emailCoVan = (sv.getLopHanhChinh() != null && sv.getLopHanhChinh().getCoVan() != null && sv.getLopHanhChinh().getCoVan().getNhanSu() != null && sv.getLopHanhChinh().getCoVan().getNhanSu().getTaiKhoan() != null)
                ? sv.getLopHanhChinh().getCoVan().getNhanSu().getTaiKhoan().getEmail() : "—";

        return new SinhVienDashboardView(
                sv.getMaSV(),
                sv.getHoTen(),
                sv.getTaiKhoan() != null ? sv.getTaiKhoan().getEmail() : "—",
                maLopHC,
                tenLopHC,
                tenNganh,
                tenKhoaHoc,
                sv.getTrangThaiHienTai(),
                tenCoVan,
                emailCoVan,
                termCode,
                termName,
                hocKys,
                registeredCredits,
                academicPolicy.minCreditsPerTerm(),
                academicPolicy.maxCreditsPerTerm(),
                totalCumulativeCredits,
                cumulativeGpa,
                academicStanding,
                currentRegistrations,
                weeklySchedule,
                recentDecisions
        );
    }
}
