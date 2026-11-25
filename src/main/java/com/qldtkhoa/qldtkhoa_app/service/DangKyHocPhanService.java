package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyDaChonRow;
import com.qldtkhoa.qldtkhoa_app.dto.DangKyItemRow;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DangKyHocPhanService {

    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final LopHocPhanRepository lopHocPhanRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DieuKienMonHocRepository dieuKienMonHocRepository;
    private final KetQuaHocTapRepository ketQuaHocTapRepository;
    private final LichGiangDayRepository lichGiangDayRepository;
    private final AcademicPolicy academicPolicy;

    public DangKyHocPhanService(
            DangKyHocPhanRepository dangKyHocPhanRepository,
            LopHocPhanRepository lopHocPhanRepository,
            SinhVienRepository sinhVienRepository,
            DieuKienMonHocRepository dieuKienMonHocRepository,
            KetQuaHocTapRepository ketQuaHocTapRepository,
            LichGiangDayRepository lichGiangDayRepository,
            AcademicPolicy academicPolicy) {
        this.dangKyHocPhanRepository = dangKyHocPhanRepository;
        this.lopHocPhanRepository = lopHocPhanRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.dieuKienMonHocRepository = dieuKienMonHocRepository;
        this.ketQuaHocTapRepository = ketQuaHocTapRepository;
        this.lichGiangDayRepository = lichGiangDayRepository;
        this.academicPolicy = academicPolicy;
    }

    public List<DangKyItemRow> getAvailableSections(String maSV, String maHK) {
        // Lấy tất cả các lớp học phần có thể mở đăng ký trong kỳ
        List<LopHocPhan> sections = lopHocPhanRepository.findByHocKy_MaHKAndTrangThaiLop(maHK, TrangThaiLop.CHO_MO);

        // Lấy danh sách các lớp sinh viên đã đăng ký hợp lệ trong kỳ
        List<DangKyHocPhan> studentRegistrations = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK)
                .stream()
                .filter(dk -> dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE)
                .toList();

        Set<String> registeredLhpIds = studentRegistrations.stream()
                .map(dk -> dk.getLopHocPhan().getMaLHP())
                .collect(Collectors.toSet());

        Set<String> registeredSubjectIds = studentRegistrations.stream()
                .map(dk -> dk.getLopHocPhan().getMonHoc().getMaMon())
                .collect(Collectors.toSet());

        return sections.stream().map(lhp -> {
            boolean daDangKy = registeredLhpIds.contains(lhp.getMaLHP());
            boolean daDangKyMonKhac = !daDangKy && registeredSubjectIds.contains(lhp.getMonHoc().getMaMon());

            long currentEnrolled = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                    lhp.getMaLHP(), List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI)
            );

            List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(lhp.getMaLHP());
            String scheduleStr = formatSchedules(schedules);

            return new DangKyItemRow(
                    lhp.getMaLHP(),
                    lhp.getMonHoc().getMaMon(),
                    lhp.getMonHoc().getTenMon(),
                    lhp.getMonHoc().getSoTinChi(),
                    (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null) ? lhp.getGiangVien().getNhanSu().getHoTen() : "Chưa phân công",
                    currentEnrolled,
                    lhp.getSiSoToiDa(),
                    scheduleStr,
                    lhp.getTrangThaiLop(),
                    daDangKy,
                    daDangKyMonKhac
            );
        }).toList();
    }

    public List<DangKyDaChonRow> getSelectedCourses(String maSV, String maHK) {
        List<DangKyHocPhan> list = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK);

        return list.stream()
                .filter(dk -> dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE)
                .map(dk -> {
                    LopHocPhan lhp = dk.getLopHocPhan();
                    List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(lhp.getMaLHP());
                    return new DangKyDaChonRow(
                            lhp.getMaLHP(),
                            lhp.getMonHoc().getMaMon(),
                            lhp.getMonHoc().getTenMon(),
                            lhp.getMonHoc().getSoTinChi(),
                            (lhp.getGiangVien() != null && lhp.getGiangVien().getNhanSu() != null) ? lhp.getGiangVien().getNhanSu().getHoTen() : "Chưa phân công",
                            formatSchedules(schedules),
                            dk.getNgayDangKy(),
                            dk.getTrangThaiDangKy(),
                            lhp.getTrangThaiLop()
                    );
                }).toList();
    }

    public List<com.qldtkhoa.qldtkhoa_app.dto.SinhVienDangKyRow> findBySinhVien(String maSV) {
        return dangKyHocPhanRepository.findBySinhVien_MaSV(maSV).stream()
                .map(dk -> new com.qldtkhoa.qldtkhoa_app.dto.SinhVienDangKyRow(
                        dk.getLopHocPhan().getMaLHP(),
                        dk.getLopHocPhan().getMonHoc().getTenMon(),
                        dk.getLopHocPhan().getMonHoc().getSoTinChi(),
                        dk.getLopHocPhan().getHocKy().getMaHK(),
                        dk.getNgayDangKy(),
                        dk.getTrangThaiDangKy()
                ))
                .toList();
    }

    @Transactional
    public void register(String maSV, String maLHP) {
        // 1. Kiểm tra trạng thái sinh viên
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));
        if (sv.getTrangThaiHienTai() != TrangThaiSinhVien.DANG_HOC) {
            throw new BusinessRuleException("Sinh viên hiện không ở trạng thái Được học (Trạng thái hiện tại: " + sv.getTrangThaiHienTai().getLabel() + ")!");
        }

        // 2. Kiểm tra lớp học phần
        LopHocPhan lhp = lopHocPhanRepository.findById(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (lhp.getTrangThaiLop() == TrangThaiLop.DA_CHOT_DANH_SACH || lhp.getTrangThaiLop() == TrangThaiLop.DA_HUY) {
            throw new BusinessRuleException("Lớp học phần hiện không mở cho đăng ký (Trạng thái: " + lhp.getTrangThaiLop().getLabel() + ")!");
        }

        String maHK = lhp.getHocKy().getMaHK();
        String maMon = lhp.getMonHoc().getMaMon();

        // 3. Kiểm tra đã đăng ký lớp này chưa
        Optional<DangKyHocPhan> existingOpt = dangKyHocPhanRepository.findById(new DangKyHocPhanId(maSV, maLHP));
        if (existingOpt.isPresent() && existingOpt.get().getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE) {
            throw new BusinessRuleException("Sinh viên đã đăng ký lớp học phần này!");
        }

        // 4. Kiểm tra cùng môn học không được đăng ký 2 lớp trong cùng học kỳ
        boolean duplicateSubject = dangKyHocPhanRepository.existsBySinhVien_MaSVAndLopHocPhan_MonHoc_MaMonAndLopHocPhan_HocKy_MaHKAndTrangThaiDangKy(
                maSV, maMon, maHK, TrangThaiDangKy.HOP_LE
        );
        if (duplicateSubject) {
            throw new BusinessRuleException("Sinh viên đã đăng ký một lớp học phần khác của môn " + lhp.getMonHoc().getTenMon() + " trong học kỳ này!");
        }

        // 5. Kiểm tra môn tiên quyết (Prerequisites)
        List<DieuKienMonHoc> prereqs = dieuKienMonHocRepository.findByMonHoc_MaMon(maMon);
        for (DieuKienMonHoc dk : prereqs) {
            String maTq = dk.getMonTienQuyet().getMaMon();
            boolean passed = ketQuaHocTapRepository.hasPassedSubject(maSV, maTq, academicPolicy.passScore());
            if (!passed) {
                throw new BusinessRuleException("Chưa thỏa mãn điều kiện môn tiên quyết: " + dk.getMonTienQuyet().getTenMon() + " (" + maTq + ")!");
            }
        }

        // 6. Kiểm tra giới hạn số tín chỉ tối đa trong kỳ
        List<DangKyHocPhan> currentDks = dangKyHocPhanRepository.findBySinhVien_MaSVAndLopHocPhan_HocKy_MaHK(maSV, maHK)
                .stream()
                .filter(dk -> dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE)
                .toList();

        int currentCredits = currentDks.stream().mapToInt(dk -> dk.getLopHocPhan().getMonHoc().getSoTinChi()).sum();
        int newCredits = currentCredits + lhp.getMonHoc().getSoTinChi();
        if (newCredits > academicPolicy.maxCreditsPerTerm()) {
            throw new BusinessRuleException("Vượt quá số tín chỉ tối đa được phép đăng ký trong kỳ (" + academicPolicy.maxCreditsPerTerm() + " TC)! Hiện tại: " + currentCredits + " TC, môn này: " + lhp.getMonHoc().getSoTinChi() + " TC.");
        }

        // 7. Kiểm tra trùng lịch học với các môn đã đăng ký
        List<LichGiangDay> newSchedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        for (DangKyHocPhan curDk : currentDks) {
            List<LichGiangDay> curSchedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(curDk.getLopHocPhan().getMaLHP());
            for (LichGiangDay sNew : newSchedules) {
                for (LichGiangDay sCur : curSchedules) {
                    if (sNew.getThuTrongTuan().equals(sCur.getThuTrongTuan())
                            && Math.max(sNew.getTietBatDau(), sCur.getTietBatDau()) <= Math.min(sNew.getTietKetThuc(), sCur.getTietKetThuc())) {
                        throw new BusinessRuleException("Trùng thời khóa biểu: Tiết " + sNew.getTietBatDau() + "-" + sNew.getTietKetThuc() +
                                " Thứ " + sNew.getThuTrongTuan() + " trùng với môn " + curDk.getLopHocPhan().getMonHoc().getTenMon() +
                                " (" + curDk.getLopHocPhan().getMaLHP() + ")!");
                    }
                }
            }
        }

        // 8. Khóa hàng và kiểm tra sĩ số lớp học phần (Pessimistic Write Lock)
        LopHocPhan lockedLhp = lopHocPhanRepository.findByIdForUpdate(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        long currentCount = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI)
        );

        if (currentCount >= lockedLhp.getSiSoToiDa()) {
            throw new BusinessRuleException("Lớp học phần đã đầy sĩ số (" + lockedLhp.getSiSoToiDa() + "/" + lockedLhp.getSiSoToiDa() + ")!");
        }

        // 9. Lưu đăng ký học phần (hoặc tái kích hoạt nếu trước đó đã rút lớp)
        if (existingOpt.isPresent()) {
            DangKyHocPhan existing = existingOpt.get();
            existing.setTrangThaiDangKy(TrangThaiDangKy.HOP_LE);
            existing.setNgayDangKy(LocalDateTime.now());
            dangKyHocPhanRepository.save(existing);
        } else {
            DangKyHocPhan dk = DangKyHocPhan.of(sv, lockedLhp);
            dangKyHocPhanRepository.save(dk);
        }
    }

    @Transactional
    public void withdraw(String maSV, String maLHP) {
        DangKyHocPhan dk = dangKyHocPhanRepository.findById(new DangKyHocPhanId(maSV, maLHP))
                .orElseThrow(() -> new ResourceNotFoundException("registration.notFound", maLHP));

        if (dk.getTrangThaiDangKy() != TrangThaiDangKy.HOP_LE) {
            throw new BusinessRuleException("Không thể rút lớp học phần này!");
        }

        // Ràng buộc 1: Kiểm tra trạng thái lớp học phần
        LopHocPhan lhp = dk.getLopHocPhan();
        if (lhp != null && lhp.getTrangThaiLop() == TrangThaiLop.DA_CHOT_DANH_SACH) {
            throw new BusinessRuleException("reg.withdraw.locked", lhp.getMaLHP());
        }
        if (lhp != null && lhp.getTrangThaiLop() == TrangThaiLop.DA_HUY) {
            throw new BusinessRuleException("reg.withdraw.cancelled", lhp.getMaLHP());
        }

        // Ràng buộc 2: Kiểm tra xem đã có điểm thi hay chưa
        Optional<KetQuaHocTap> kqOpt = ketQuaHocTapRepository.findById_MaSVAndId_MaLHP(maSV, maLHP);
        if (kqOpt.isPresent() && (kqOpt.get().getDiemTong() != null || kqOpt.get().getDiemCuoiKy() != null)) {
            throw new BusinessRuleException("reg.withdrawGraded");
        }

        dk.setTrangThaiDangKy(TrangThaiDangKy.DA_RUT_LOP);
        dangKyHocPhanRepository.save(dk);
    }

    private String formatSchedules(List<LichGiangDay> schedules) {
        if (schedules.isEmpty()) return "Chưa xếp lịch";
        return schedules.stream()
                .map(s -> (s.getThuTrongTuan() == 8 ? "CN" : "T" + s.getThuTrongTuan()) + " (" + s.getTietBatDau() + "-" + s.getTietKetThuc() + ") " + s.getPhongHoc())
                .collect(Collectors.joining("; "));
    }
}
