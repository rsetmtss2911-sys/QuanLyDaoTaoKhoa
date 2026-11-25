package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.BangDiemLopForm;
import com.qldtkhoa.qldtkhoa_app.dto.BangDiemSinhVienResponse;
import com.qldtkhoa.qldtkhoa_app.dto.DiemEntryForm;
import com.qldtkhoa.qldtkhoa_app.dto.DiemRow;
import com.qldtkhoa.qldtkhoa_app.entity.DangKyHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.KetQuaHocTap;
import com.qldtkhoa.qldtkhoa_app.entity.LopHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.id.DangKyHocPhanId;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.DangKyHocPhanRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KetQuaHocTapRepository;
import com.qldtkhoa.qldtkhoa_app.repository.LopHocPhanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final AcademicPolicy academicPolicy;

    public BangDiemSinhVienResponse getTranscriptView(String maSV) {
        List<BangDiemSinhVienView> list = bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(maSV);

        Map<String, List<BangDiemSinhVienView>> byTerm = list.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getTenHK() + " (" + v.getNamHoc() + ")",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        double passScore = academicPolicy.passScore();
        int tongTCTichLuy = 0;
        double tongDiemHe4NhanTC = 0.0;
        for (BangDiemSinhVienView v : list) {
            if (v.getDiemTong() != null && v.getDiemTong() >= passScore && v.getDiemHe4() != null) {
                tongTCTichLuy += v.getSoTinChi();
                tongDiemHe4NhanTC += (v.getDiemHe4() * v.getSoTinChi());
            }
        }

        Double cpa4 = tongTCTichLuy > 0 ? Math.round((tongDiemHe4NhanTC / tongTCTichLuy) * 100.0) / 100.0 : null;

        String xepLoai = "—";
        if (cpa4 != null) {
            if (cpa4 >= 3.6) xepLoai = "Xuất sắc";
            else if (cpa4 >= 3.2) xepLoai = "Giỏi";
            else if (cpa4 >= 2.5) xepLoai = "Khá";
            else if (cpa4 >= 2.0) xepLoai = "Trung bình";
            else xepLoai = "Yếu";
        }

        return new BangDiemSinhVienResponse(byTerm, tongTCTichLuy, cpa4, xepLoai);
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

        List<BangDiemSinhVienView> views = bangDiemViewRepository.findById_MaLHPOrderByHoTenAsc(maLHP);
        if (!views.isEmpty()) {
            return views.stream().map(v -> new DiemRow(
                    v.getMaSV(),
                    v.getHoTen(),
                    lopHcMap.getOrDefault(v.getMaSV(), "—"),
                    v.getTrangThaiDangKy(),
                    v.getDiemQuaTrinh(),
                    v.getDiemGiuaKy(),
                    v.getDiemCuoiKy(),
                    v.getDiemTong(),
                    v.getDiemHe4(),
                    v.getDiemChu(),
                    v.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI)).toList();
        }

        Map<String, KetQuaHocTap> gradeMap = ketQuaHocTapRepository.findById_MaLHP(maLHP).stream()
                .collect(Collectors.toMap(k -> k.getId().getMaSV(), Function.identity()));

        return registrations.stream()
                .filter(dk -> dk.getTrangThaiDangKy() != TrangThaiDangKy.DA_RUT_LOP)
                .map(dk -> {
                    KetQuaHocTap kq = gradeMap.get(dk.getSinhVien().getMaSV());
                    Double qt = kq != null ? kq.getDiemQuaTrinh() : null;
                    Double gk = kq != null ? kq.getDiemGiuaKy() : null;
                    Double ck = kq != null ? kq.getDiemCuoiKy() : null;
                    Double tong = kq != null ? kq.getDiemTong() : null;

                    String maLopHC = (dk.getSinhVien() != null && dk.getSinhVien().getLopHanhChinh() != null)
                            ? dk.getSinhVien().getLopHanhChinh().getMaLopHC()
                            : "—";

                    return new DiemRow(
                            dk.getSinhVien().getMaSV(),
                            dk.getSinhVien().getHoTen(),
                            maLopHC,
                            dk.getTrangThaiDangKy(),
                            qt, gk, ck, tong,
                            calculateGpa4(tong),
                            calculateLetter(tong),
                            dk.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI);
                }).toList();
    }

    @Transactional
    public void saveGradeSheet(String maLHP, BangDiemLopForm form, String lecturerMaNS, boolean isAdmin) {
        LopHocPhan lhp = lopHocPhanRepository.findById(maLHP)
                .orElseThrow(() -> new ResourceNotFoundException("lophp.notFound", maLHP));

        // Kiểm tra quyền sở hữu (Record Ownership): Chỉ giảng viên được phân công mới
        // được nhập điểm
        if (!isAdmin && !lhp.getGiangVien().getMaNS().equalsIgnoreCase(lecturerMaNS)) {
            throw new AccessDeniedException("Bạn không phải là giảng viên phụ trách lớp học phần này!");
        }

        for (DiemEntryForm entry : form.getEntries()) {
            String maSV = entry.getMaSV();
            DangKyHocPhan dk = dangKyHocPhanRepository.findById(new DangKyHocPhanId(maSV, maLHP))
                    .orElseThrow(() -> new ResourceNotFoundException("registration.notFound", maSV));

            if (dk.getTrangThaiDangKy() == TrangThaiDangKy.DA_RUT_LOP) {
                throw new BusinessRuleException("Không thể nhập điểm cho sinh viên đã rút lớp (" + maSV + ")!");
            }

            // Xử lý Cấm thi (Mã lỗi 50011: DiemCuoiKy phải là NULL)
            if (entry.isCamThi()) {
                dk.setTrangThaiDangKy(TrangThaiDangKy.CAM_THI);
                entry.setDiemCuoiKy(null);
            } else {
                dk.setTrangThaiDangKy(TrangThaiDangKy.HOP_LE);
            }
            dangKyHocPhanRepository.save(dk);

            validateGradeBounds(entry.getDiemQuaTrinh());
            validateGradeBounds(entry.getDiemGiuaKy());
            validateGradeBounds(entry.getDiemCuoiKy());

            KetQuaHocTap kq = ketQuaHocTapRepository.findById_MaSVAndId_MaLHP(maSV, maLHP)
                    .orElseGet(() -> new KetQuaHocTap(dk, null, null, null));

            kq.setDiemQuaTrinh(entry.getDiemQuaTrinh());
            kq.setDiemGiuaKy(entry.getDiemGiuaKy());
            kq.setDiemCuoiKy(entry.getDiemCuoiKy());

            ketQuaHocTapRepository.save(kq);
        }
    }

    private void validateGradeBounds(Double diem) {
        if (diem != null && (diem < 0.0 || diem > 10.0)) {
            throw new BusinessRuleException("Điểm số phải nằm trong thang điểm từ 0.0 đến 10.0!");
        }
    }

    public static String calculateLetter(Double diemTong) {
        if (diemTong == null)
            return "—";
        if (diemTong >= 8.5)
            return "A";
        if (diemTong >= 8.0)
            return "B+";
        if (diemTong >= 7.0)
            return "B";
        if (diemTong >= 6.5)
            return "C+";
        if (diemTong >= 5.5)
            return "C";
        if (diemTong >= 5.0)
            return "D+";
        if (diemTong >= 4.0)
            return "D";
        return "F";
    }

    public static Double calculateGpa4(Double diemTong) {
        if (diemTong == null)
            return null;
        if (diemTong >= 8.5)
            return 4.0;
        if (diemTong >= 8.0)
            return 3.5;
        if (diemTong >= 7.0)
            return 3.0;
        if (diemTong >= 6.5)
            return 2.5;
        if (diemTong >= 5.5)
            return 2.0;
        if (diemTong >= 5.0)
            return 1.5;
        if (diemTong >= 4.0)
            return 1.0;
        return 0.0;
    }
}
