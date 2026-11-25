package com.qldtkhoa.qldtkhoa_app.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LopHocPhanService {

    private final LopHocPhanRepository lopHocPhanRepository;
    private final LichGiangDayRepository lichGiangDayRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final HocKyNamHocRepository hocKyNamHocRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;

    public Page<LopHocPhanRow> search(String q, String maHK, String maMon, String maGV, TrangThaiLop status, Pageable pageable) {
        return lopHocPhanRepository.search(q == null ? "" : q.trim(), maHK, maMon, maGV, status, pageable)
                .map(this::toRow);
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

    public List<LopHocPhanRow> findByLecturerAndTerm(String maNS, String maHK) {
        List<LopHocPhan> sections = (maHK != null && !maHK.isBlank())
                ? lopHocPhanRepository.findByHocKy_MaHKAndGiangVien_MaNS(maHK, maNS)
                : lopHocPhanRepository.findByGiangVien_MaNS(maNS);
        return sections.stream().map(this::toRow).toList();
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
                form.getTrangThaiLop() != null ? form.getTrangThaiLop() : TrangThaiLop.CHO_MO
        );
        lopHocPhanRepository.save(lhp);
    }

    @Transactional
    public void update(String maLHP, LopHocPhanForm form) {
        LopHocPhan lhp = getOrThrow(maLHP);

        GiangVien gv = giangVienRepository.findById(form.getMaGV())
                .orElseThrow(() -> new ResourceNotFoundException("nhansu.notFound", form.getMaGV()));

        lhp.setGiangVien(gv);
        lhp.setSiSoToiDa(form.getSiSoToiDa());
        lhp.setTrangThaiLop(form.getTrangThaiLop());
    }

    @Transactional
    public void delete(String maLHP) {
        getOrThrow(maLHP);
        long count = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                maLHP, List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI)
        );
        if (count > 0) {
            throw new BusinessRuleException("Lớp học phần đã có sinh viên đăng ký (" + count + " SV), không thể xóa!");
        }

        List<LichGiangDay> schedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        lichGiangDayRepository.deleteAll(schedules);

        lopHocPhanRepository.deleteById(maLHP);
    }

    @Transactional
    public void addSchedule(String maLHP, LichGiangDayForm form) {
        LopHocPhan lhp = getOrThrow(maLHP);

        if (lhp.getTrangThaiLop() == TrangThaiLop.DA_HUY) {
            throw new BusinessRuleException("Không thể xếp lịch cho lớp học phần đã bị hủy!");
        }

        if (form.getTietBatDau() == null || form.getTietKetThuc() == null ||
                form.getTietBatDau() < 1 || form.getTietKetThuc() > 15 ||
                form.getTietBatDau() > form.getTietKetThuc()) {
            throw new BusinessRuleException("Tiết học không hợp lệ! Tiết học phải nằm trong khoảng từ 1 đến 15 và Tiết bắt đầu <= Tiết kết thúc.");
        }

        if (form.getThuTrongTuan() == null || form.getThuTrongTuan() < 2 || form.getThuTrongTuan() > 8) {
            throw new BusinessRuleException("Thứ trong tuần không hợp lệ (phải từ Thứ 2 đến Chủ nhật)!");
        }

        int newThu = form.getThuTrongTuan();
        int newStart = form.getTietBatDau();
        int newEnd = form.getTietKetThuc();
        String newRoom = form.getPhongHoc().trim();
        String maHK = lhp.getHocKy().getMaHK();
        String maGV = lhp.getGiangVien() != null ? lhp.getGiangVien().getMaNS() : null;

        // 1. Kiểm tra trùng lịch ngay trong chính lớp này
        List<LichGiangDay> currentSchedules = lichGiangDayRepository.findByLopHocPhan_MaLHPOrderByThuTrongTuanAscTietBatDauAsc(maLHP);
        for (LichGiangDay ex : currentSchedules) {
            if (ex.getThuTrongTuan().equals(newThu) && isOverlap(newStart, newEnd, ex.getTietBatDau(), ex.getTietKetThuc())) {
                throw new BusinessRuleException("Lớp học phần đã có tiết học trùng vào Thứ " + newThu + " (Tiết " + ex.getTietBatDau() + "-" + ex.getTietKetThuc() + ")!");
            }
        }

        // 2. Kiểm tra Xung đột phòng học (Room Conflict - Mã lỗi 50020)
        List<LichGiangDay> roomSchedules = lichGiangDayRepository.findByLopHocPhan_HocKy_MaHKAndPhongHoc(maHK, newRoom);
        for (LichGiangDay ex : roomSchedules) {
            if (!ex.getLopHocPhan().getMaLHP().equals(maLHP)
                    && ex.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY
                    && ex.getThuTrongTuan().equals(newThu)
                    && isOverlap(newStart, newEnd, ex.getTietBatDau(), ex.getTietKetThuc())) {
                throw new BusinessRuleException("Xung đột phòng học: Phòng " + newRoom + " đã được xếp cho lớp " +
                        ex.getLopHocPhan().getMaLHP() + " vào Thứ " + newThu + " (Tiết " + ex.getTietBatDau() + "-" + ex.getTietKetThuc() + ")!");
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
                    throw new BusinessRuleException("Xung đột lịch giảng dạy: Giảng viên " + tenGV +
                            " đã có lịch dạy lớp " + ex.getLopHocPhan().getMaLHP() + " vào Thứ " + newThu + " (Tiết " + ex.getTietBatDau() + "-" + ex.getTietKetThuc() + ")!");
                }
            }
        }

        LichGiangDay lgd = new LichGiangDay(lhp, newThu, newStart, newEnd, newRoom);
        lichGiangDayRepository.save(lgd);
    }

    @Transactional
    public void removeSchedule(String maLHP, Integer thuTrongTuan, Integer tietBatDau) {
        LichGiangDayId id = new LichGiangDayId(maLHP, thuTrongTuan, tietBatDau);
        if (lichGiangDayRepository.existsById(id)) {
            lichGiangDayRepository.deleteById(id);
        }
    }

    private boolean isOverlap(int start1, int end1, int start2, int end2) {
        return Math.max(start1, start2) <= Math.min(end1, end2);
    }

    public LopHocPhanRow toRow(LopHocPhan lhp) {
        long currentEnrolled = dangKyHocPhanRepository.countByLopHocPhan_MaLHPAndTrangThaiDangKyIn(
                lhp.getMaLHP(), List.of(TrangThaiDangKy.HOP_LE, TrangThaiDangKy.CAM_THI)
        );

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
