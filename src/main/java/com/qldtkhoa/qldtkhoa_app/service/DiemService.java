package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KetQuaHocTapRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.util.CsvExportUtils;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiemService {

    private final LopHocPhanRepository lopHocPhanRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final KetQuaHocTapRepository ketQuaHocTapRepository;
    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
    private final HocLucService hocLucService;
    private final AuditService auditService;

    public BangDiemSinhVienResponse getTranscriptView(String maSV) {
        return hocLucService.evaluateStudent(maSV);
    }

    public List<DiemRow> getGradeSheet(String maLHP) {
        List<DangKyHocPhan> registrations = dangKyHocPhanRepository
                .findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(maLHP);
        Map<String, String> lopHcMap = registrations.stream()
                .filter(dk -> dk.getSinhVien() != null && dk.getSinhVien().getLopHanhChinh() != null)
                .collect(Collectors.toMap(
                        dk -> dk.getSinhVien().getMaSV(),
                        dk -> dk.getSinhVien().getLopHanhChinh().getMaLopHC(),
                        (existing, replacement) -> existing
                ));
        Map<String, String> lyDoMap = registrations.stream()
                .filter(dk -> dk.getSinhVien() != null && dk.getLyDoCamThi() != null)
                .collect(Collectors.toMap(
                        dk -> dk.getSinhVien().getMaSV(),
                        dk -> dk.getLyDoCamThi(),
                        (existing, replacement) -> existing
                ));

        List<BangDiemSinhVienView> views = bangDiemViewRepository.findById_MaLHPOrderByHoTenAsc(maLHP);
        if (!views.isEmpty()) {
            return views.stream().map(v -> {
                boolean camThi = v.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI;
                Double tong = camThi ? Double.valueOf(0.0) : v.getDiemTong();
                Double he4 = camThi ? Double.valueOf(0.0)
                        : (v.getDiemHe4() != null ? v.getDiemHe4() : GradeScale.toGpa4(tong));
                String chu = camThi ? "F" : (v.getDiemChu() != null ? v.getDiemChu() : GradeScale.toLetter(tong));
                return new DiemRow(
                        v.getMaSV(),
                        v.getHoTen(),
                        lopHcMap.getOrDefault(v.getMaSV(), "—"),
                        v.getTrangThaiDangKy(),
                        v.getDiemQuaTrinh(),
                        v.getDiemGiuaKy(),
                        v.getDiemCuoiKy(),
                        tong,
                        he4,
                        chu,
                        camThi,
                        v.getLyDoCamThi() != null ? v.getLyDoCamThi() : lyDoMap.get(v.getMaSV())
                );
            }).toList();
        }

        Map<String, KetQuaHocTap> gradeMap = ketQuaHocTapRepository.findById_MaLHP(maLHP).stream()
                .collect(Collectors.toMap(k -> k.getId().getMaSV(), Function.identity(), (a, b) -> a));

        return registrations.stream()
                .filter(dk -> dk.getTrangThaiDangKy() != TrangThaiDangKy.DA_RUT_LOP)
                .map(dk -> {
                    KetQuaHocTap kq = gradeMap.get(dk.getSinhVien().getMaSV());
                    Double qt = kq != null ? kq.getDiemQuaTrinh() : null;
                    Double gk = kq != null ? kq.getDiemGiuaKy() : null;
                    Double ck = kq != null ? kq.getDiemCuoiKy() : null;
                    boolean camThi = dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI;
                    MonHoc mon = dk.getLopHocPhan() != null ? dk.getLopHocPhan().getMonHoc() : null;
                    Double tong = camThi ? Double.valueOf(0.0)
                            : (kq != null && kq.getDiemTong() != null
                                ? kq.getDiemTong()
                                : (kq != null && mon != null
                                    ? GradeScale.calculateTotal(qt, gk, ck, mon.getTrongSoQT(), mon.getTrongSoGK(), mon.getTrongSoCK())
                                    : null));

                    String maLopHC = (dk.getSinhVien() != null && dk.getSinhVien().getLopHanhChinh() != null)
                            ? dk.getSinhVien().getLopHanhChinh().getMaLopHC()
                            : "—";

                    return new DiemRow(
                            dk.getSinhVien().getMaSV(),
                            dk.getSinhVien().getHoTen(),
                            maLopHC,
                            dk.getTrangThaiDangKy(),
                            qt, gk, ck, tong,
                            camThi ? Double.valueOf(0.0) : calculateGpa4(tong),
                            camThi ? "F" : calculateLetter(tong),
                            camThi,
                            dk.getLyDoCamThi());
                }).toList();
    }

    /**
     * Lưu bảng điểm lớp học phần.
     *
     * @param isTrainingOffice {@code true} khi Phòng Đào tạo hiệu chỉnh (được phép sửa cả lớp {@code DA_KHOA_DIEM}
     *                         nhưng bắt buộc nhập lý do và ghi {@code NhatKyHeThong} theo GATE 3c);
     *                         {@code false} khi Giảng viên phụ trách chấm điểm (chỉ sửa được khi {@code DA_CHOT_DANH_SACH}).
     */
    @Transactional
    public void saveGradeSheet(String maLHP, BangDiemLopForm form, String actorId, boolean isTrainingOffice) {
        LopHocPhan lhp = lopHocPhanRepository.findById(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        if (!isTrainingOffice && (lhp.getGiangVien() == null || !lhp.getGiangVien().getMaNS().equalsIgnoreCase(actorId))) {
            throw new AccessDeniedException("Bạn không phải là giảng viên phụ trách lớp học phần này!");
        }

        TrangThaiLop sectionStatus = lhp.getTrangThaiLop();
        if (!isTrainingOffice) {
            if (sectionStatus == TrangThaiLop.DA_KHOA_DIEM) {
                throw new BusinessRuleException("grade.classGradesLocked");
            }
            if (sectionStatus != TrangThaiLop.DA_CHOT_DANH_SACH) {
                throw new BusinessRuleException("grade.classNotLocked");
            }
        } else if (sectionStatus == TrangThaiLop.CHO_MO || sectionStatus == TrangThaiLop.DA_HUY) {
            throw new BusinessRuleException("grade.classNotLocked");
        }

        boolean editingLockedGrades = isTrainingOffice && sectionStatus == TrangThaiLop.DA_KHOA_DIEM;
        String editReason = form != null && form.getLyDoChinhSua() != null ? form.getLyDoChinhSua().trim() : "";
        if (editingLockedGrades && editReason.isEmpty()) {
            throw new BusinessRuleException("grade.editReason.required");
        }

        if (form == null || form.getEntries() == null || form.getEntries().isEmpty()) {
            return;
        }

        Map<String, DangKyHocPhan> regMap = dangKyHocPhanRepository
                .findByLopHocPhan_MaLHPOrderBySinhVien_HoTenAsc(maLHP).stream()
                .collect(Collectors.toMap(
                        dk -> dk.getId().getMaSV(),
                        Function.identity(),
                        (a, b) -> a
                ));

        Map<String, KetQuaHocTap> gradeMap = ketQuaHocTapRepository.findById_MaLHP(maLHP).stream()
                .collect(Collectors.toMap(
                        kq -> kq.getId().getMaSV(),
                        Function.identity(),
                        (a, b) -> a
                ));

        List<DangKyHocPhan> updatedRegistrations = new ArrayList<>();
        List<KetQuaHocTap> updatedGrades = new ArrayList<>();
        List<PendingAudit> pendingAudits = new ArrayList<>();

        for (DiemEntryForm entry : form.getEntries()) {
            String maSV = entry.getMaSV();
            DangKyHocPhan dk = regMap.get(maSV);
            if (dk == null) {
                throw new ResourceNotFoundException("registration.notFound", maSV);
            }

            if (dk.getTrangThaiDangKy() == TrangThaiDangKy.DA_RUT_LOP) {
                throw new BusinessRuleException("grade.error.withdrawn", maSV);
            }

            TrangThaiDangKy oldRegStatus = dk.getTrangThaiDangKy();
            String oldBanReason = dk.getLyDoCamThi();

            if (entry.isCamThi()) {
                dk.setTrangThaiDangKy(TrangThaiDangKy.CAM_THI);
                String banReason = entry.getLyDoCamThi() != null ? entry.getLyDoCamThi().trim() : null;
                dk.setLyDoCamThi(banReason != null && !banReason.isEmpty() ? banReason : null);
                entry.setDiemCuoiKy(null);
            } else {
                dk.setTrangThaiDangKy(TrangThaiDangKy.HOP_LE);
                dk.setLyDoCamThi(null);
            }
            updatedRegistrations.add(dk);

            validateGradeBounds(entry.getDiemQuaTrinh());
            validateGradeBounds(entry.getDiemGiuaKy());
            validateGradeBounds(entry.getDiemCuoiKy());

            KetQuaHocTap existing = gradeMap.get(maSV);
            Double oldQt = existing != null ? existing.getDiemQuaTrinh() : null;
            Double oldGk = existing != null ? existing.getDiemGiuaKy() : null;
            Double oldCk = existing != null ? existing.getDiemCuoiKy() : null;

            boolean statusChanged = oldRegStatus != dk.getTrangThaiDangKy()
                    || !Objects.equals(oldBanReason, dk.getLyDoCamThi());
            boolean gradesChanged = !Objects.equals(oldQt, entry.getDiemQuaTrinh())
                    || !Objects.equals(oldGk, entry.getDiemGiuaKy())
                    || !Objects.equals(oldCk, entry.getDiemCuoiKy());

            if (editingLockedGrades && (statusChanged || gradesChanged)) {
                String oldSnap = formatSnapshot(oldRegStatus, oldQt, oldGk, oldCk);
                String newSnap = formatSnapshot(dk.getTrangThaiDangKy(), entry.getDiemQuaTrinh(),
                        entry.getDiemGiuaKy(), entry.getDiemCuoiKy());
                pendingAudits.add(new PendingAudit("EDIT_LOCKED_GRADE", "KetQuaHocTap",
                        maLHP + ":" + maSV, oldSnap, newSnap, editReason));
            } else if (oldRegStatus != dk.getTrangThaiDangKy()) {
                pendingAudits.add(new PendingAudit("TOGGLE_EXAM_BAN", "DangKyHocPhan",
                        maLHP + ":" + maSV, oldRegStatus.getLabel(), dk.getTrangThaiDangKy().getLabel(),
                        dk.getLyDoCamThi()));
            }

            boolean allNull = entry.getDiemQuaTrinh() == null
                    && entry.getDiemGiuaKy() == null
                    && entry.getDiemCuoiKy() == null;
            if (existing == null && allNull) {
                continue;
            }

            KetQuaHocTap kq = existing != null ? existing : new KetQuaHocTap(dk, null, null, null);
            kq.setDiemQuaTrinh(entry.getDiemQuaTrinh());
            kq.setDiemGiuaKy(entry.getDiemGiuaKy());
            kq.setDiemCuoiKy(entry.getDiemCuoiKy());
            updatedGrades.add(kq);
        }

        // Bắt buộc flush UPDATE DangKyHocPhan trước KetQuaHocTap để tránh lỗi Trigger 50011 khi bỏ Cấm thi + nhập CK
        if (!updatedRegistrations.isEmpty()) {
            dangKyHocPhanRepository.saveAllAndFlush(updatedRegistrations);
        }
        if (!updatedGrades.isEmpty()) {
            ketQuaHocTapRepository.saveAllAndFlush(updatedGrades);
        }

        for (PendingAudit audit : pendingAudits) {
            auditService.log(audit.hanhDong(), audit.doiTuong(), audit.maBanGhi(),
                    audit.giaTriCu(), audit.giaTriMoi(), audit.lyDo());
        }
    }

    private String formatSnapshot(TrangThaiDangKy status, Double qt, Double gk, Double ck) {
        return (status != null ? status.getLabel() : "—")
                + " | QT=" + qt + ", GK=" + gk + ", CK=" + ck;
    }

    private void validateGradeBounds(Double diem) {
        if (diem != null && (!Double.isFinite(diem) || diem < 0.0 || diem > 10.0)) {
            throw new BusinessRuleException("grade.scoreRange");
        }
    }

    public static String calculateLetter(Double diemTong) {
        return GradeScale.toLetter(diemTong);
    }

    public static Double calculateGpa4(Double diemTong) {
        return GradeScale.toGpa4(diemTong);
    }

    public ResponseEntity<byte[]> exportGradeSheetCsv(String maLHP) {
        List<DiemRow> rows = getGradeSheet(maLHP);
        List<String> headers = List.of(
                "MaSV", "HoTen", "LopHC", "TrangThai", "LyDoCamThi",
                "DiemQT", "DiemGK", "DiemCK", "DiemTong", "DiemHe4", "DiemChu"
        );
        List<List<String>> csvRows = rows.stream()
                .map(r -> List.of(
                        str(r.maSV()),
                        str(r.hoTen()),
                        str(r.maLopHC()),
                        r.trangThaiDangKy() != null ? r.trangThaiDangKy().getLabel() : "",
                        str(r.lyDoCamThi()),
                        num(r.diemQuaTrinh()),
                        num(r.diemGiuaKy()),
                        num(r.diemCuoiKy()),
                        num(r.diemTong()),
                        num(r.diemHe4()),
                        str(r.diemChu())
                ))
                .toList();
        return CsvExportUtils.csvResponse("bang-diem-" + maLHP + ".csv", headers, csvRows);
    }

    private static String str(String val) {
        return val != null ? val : "";
    }

    private static String num(Double val) {
        return val != null ? String.valueOf(val) : "";
    }

    private record PendingAudit(
            String hanhDong,
            String doiTuong,
            String maBanGhi,
            String giaTriCu,
            String giaTriMoi,
            String lyDo
    ) {
    }
}
