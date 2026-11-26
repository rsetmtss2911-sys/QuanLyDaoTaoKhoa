package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTForm;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTRow;
import com.qldtkhoa.qldtkhoa_app.dto.KhoiProgressRow;
import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.entity.*;
import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import com.qldtkhoa.qldtkhoa_app.entity.id.ChuongTrinhDaoTaoId;
import com.qldtkhoa.qldtkhoa_app.entity.id.DinhMucCTDTId;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChuongTrinhDaoTaoService {

    private final ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;
    private final DinhMucCTDTRepository dinhMucCTDTRepository;
    private final NganhHocRepository nganhHocRepository;
    private final KhoaNhapHocRepository khoaNhapHocRepository;
    private final MonHocRepository monHocRepository;
    private final SinhVienRepository sinhVienRepository;
    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
    private final DangKyHocPhanRepository dangKyHocPhanRepository;
    private final AcademicPolicy academicPolicy;

    public TienDoHocTapView getTienDoCTDT(String maSV) {
        SinhVien sv = sinhVienRepository.findByIdWithDetails(maSV)
                .or(() -> sinhVienRepository.findById(maSV))
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));

        String maNganh = sv.getLopHanhChinh().getNganhHoc().getMaNganh();
        String tenNganh = sv.getLopHanhChinh().getNganhHoc().getTenNganh();
        String maKhoaHoc = sv.getLopHanhChinh().getKhoaNhapHoc().getMaKhoaHoc();

        List<CTDTRow> curriculum = findByNganhAndKhoa(maNganh, maKhoaHoc);
        List<DinhMucCTDT> quotas = dinhMucCTDTRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc(maNganh, maKhoaHoc);
        Map<KhoiKienThuc, Integer> quotaMap = new EnumMap<>(KhoiKienThuc.class);
        for (DinhMucCTDT dm : quotas) {
            if (dm.getKhoiKienThuc() != null) {
                quotaMap.put(dm.getKhoiKienThuc(), dm.getTinChiTuChonToiThieu() != null ? dm.getTinChiTuChonToiThieu() : 0);
            }
        }

        List<BangDiemSinhVienView> grades = bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(maSV);

        double passScore = academicPolicy.passScore();
        Set<String> passedSubjects = new HashSet<>();
        Set<String> attemptedFailedSubjects = new HashSet<>();
        for (BangDiemSinhVienView g : grades) {
            if (g.getTrangThaiLop() == TrangThaiLop.DA_HUY) {
                continue;
            }
            String maMon = g.getMaMon();
            if (maMon == null) {
                continue;
            }
            if (g.getTrangThaiDangKy() == TrangThaiDangKy.CAM_THI) {
                attemptedFailedSubjects.add(maMon);
            } else if (g.getDiemTong() != null) {
                if (g.getDiemTong() >= passScore) {
                    passedSubjects.add(maMon);
                } else {
                    attemptedFailedSubjects.add(maMon);
                }
            }
        }

        Set<String> currentlyEnrolledSubjects = dangKyHocPhanRepository.findBySinhVien_MaSV(maSV).stream()
                .filter(dk -> dk.getTrangThaiDangKy() == TrangThaiDangKy.HOP_LE
                        && dk.getLopHocPhan() != null
                        && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_HUY
                        && dk.getLopHocPhan().getTrangThaiLop() != TrangThaiLop.DA_KHOA_DIEM)
                .map(dk -> dk.getLopHocPhan().getMonHoc().getMaMon())
                .collect(Collectors.toSet());

        Map<String, String> courseStatuses = new LinkedHashMap<>();
        for (CTDTRow c : curriculum) {
            String maMon = c.maMon();
            if (passedSubjects.contains(maMon)) {
                courseStatuses.put(maMon, "DA_TICH_LUY");
            } else if (currentlyEnrolledSubjects.contains(maMon)) {
                courseStatuses.put(maMon, "DANG_HOC");
            } else if (attemptedFailedSubjects.contains(maMon)) {
                courseStatuses.put(maMon, "CHUA_DAT");
            } else {
                courseStatuses.put(maMon, "CHUA_HOC");
            }
        }

        boolean hasExplicitQuotas = !quotaMap.isEmpty();
        List<KhoiProgressRow> blockProgress = new ArrayList<>();
        int totalCurriculumCredits = 0;
        int completedCredits = 0;

        for (KhoiKienThuc khoi : KhoiKienThuc.values()) {
            int reqMandatory = 0;
            int earnedMandatory = 0;
            int sumElectiveInCatalog = 0;
            int rawEarnedElective = 0;
            boolean hasAnyCourseInBlock = false;

            for (CTDTRow c : curriculum) {
                if (c.khoiKienThuc() != khoi) {
                    continue;
                }
                hasAnyCourseInBlock = true;
                int tc = c.soTinChi() != null ? c.soTinChi() : 0;
                boolean passed = passedSubjects.contains(c.maMon());
                if (c.loaiHocPhan() == LoaiHocPhan.TU_CHON) {
                    sumElectiveInCatalog += tc;
                    if (passed) {
                        rawEarnedElective += tc;
                    }
                } else {
                    reqMandatory += tc;
                    if (passed) {
                        earnedMandatory += tc;
                    }
                }
            }

            int minElective = hasExplicitQuotas
                    ? quotaMap.getOrDefault(khoi, 0)
                    : sumElectiveInCatalog;

            if (!hasAnyCourseInBlock && minElective == 0) {
                continue;
            }

            int earnedElective = minElective > 0 ? Math.min(rawEarnedElective, minElective) : 0;
            int blockTotalRequired = reqMandatory + minElective;
            int blockTotalEarned = earnedMandatory + earnedElective;
            int blockPercent = blockTotalRequired > 0
                    ? Math.min(100, blockTotalEarned * 100 / blockTotalRequired)
                    : 0;

            blockProgress.add(new KhoiProgressRow(
                    khoi, reqMandatory, earnedMandatory, minElective, earnedElective,
                    blockTotalRequired, blockTotalEarned, blockPercent
            ));

            totalCurriculumCredits += blockTotalRequired;
            completedCredits += blockTotalEarned;
        }

        int progressPercent = totalCurriculumCredits > 0
                ? Math.min(100, completedCredits * 100 / totalCurriculumCredits)
                : 0;

        return new TienDoHocTapView(
                sv.getMaSV(),
                sv.getHoTen(),
                tenNganh,
                maKhoaHoc,
                totalCurriculumCredits,
                completedCredits,
                progressPercent,
                curriculum,
                passedSubjects,
                blockProgress,
                courseStatuses
        );
    }

    public List<CTDTRow> findByNganhAndKhoa(String maNganh, String maKhoaHoc) {
        if (maNganh == null || maKhoaHoc == null || maNganh.isBlank() || maKhoaHoc.isBlank()) {
            return List.of();
        }
        return chuongTrinhDaoTaoRepository
                .findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc(maNganh, maKhoaHoc)
                .stream()
                .map(this::toRow)
                .toList();
    }

    public Map<KhoiKienThuc, Integer> getQuotas(String maNganh, String maKhoaHoc) {
        Map<KhoiKienThuc, Integer> map = new EnumMap<>(KhoiKienThuc.class);
        for (KhoiKienThuc k : KhoiKienThuc.values()) {
            map.put(k, 0);
        }
        if (maNganh != null && maKhoaHoc != null && !maNganh.isBlank() && !maKhoaHoc.isBlank()) {
            for (DinhMucCTDT dm : dinhMucCTDTRepository.findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc(maNganh, maKhoaHoc)) {
                if (dm.getKhoiKienThuc() != null) {
                    map.put(dm.getKhoiKienThuc(), dm.getTinChiTuChonToiThieu() != null ? dm.getTinChiTuChonToiThieu() : 0);
                }
            }
        }
        return map;
    }

    @Transactional
    public void saveQuotas(String maNganh, String maKhoaHoc, Integer daiCuong, Integer coSoNganh, Integer chuyenNganh) {
        NganhHoc nganh = nganhHocRepository.findById(maNganh)
                .orElseThrow(() -> new ResourceNotFoundException("nganhhoc.notFound", maNganh));
        KhoaNhapHoc khoa = khoaNhapHocRepository.findById(maKhoaHoc)
                .orElseThrow(() -> new ResourceNotFoundException("khoanhaphoc.notFound", maKhoaHoc));

        saveSingleQuota(nganh, khoa, KhoiKienThuc.DAI_CUONG, daiCuong);
        saveSingleQuota(nganh, khoa, KhoiKienThuc.CO_SO_NGANH, coSoNganh);
        saveSingleQuota(nganh, khoa, KhoiKienThuc.CHUYEN_NGANH, chuyenNganh);
    }

    private void saveSingleQuota(NganhHoc nganh, KhoaNhapHoc khoa, KhoiKienThuc khoi, Integer value) {
        int val = value != null ? value : 0;
        if (val < 0) {
            throw new BusinessRuleException("ctdt.quota.negative");
        }
        DinhMucCTDTId id = new DinhMucCTDTId(nganh.getMaNganh(), khoa.getMaKhoaHoc(), khoi);
        DinhMucCTDT entity = dinhMucCTDTRepository.findById(id).orElse(null);
        if (entity == null) {
            entity = new DinhMucCTDT(nganh, khoa, khoi, val);
        } else {
            entity.setTinChiTuChonToiThieu(val);
        }
        dinhMucCTDTRepository.save(entity);
    }

    @Transactional
    public void addCourse(CTDTForm form) {
        ChuongTrinhDaoTaoId id = new ChuongTrinhDaoTaoId(form.getMaNganh(), form.getMaKhoaHoc(), form.getMaMon());
        if (chuongTrinhDaoTaoRepository.existsById(id)) {
            throw new DuplicateResourceException("ctdt.duplicate", form.getMaMon());
        }

        NganhHoc nganh = nganhHocRepository.findById(form.getMaNganh())
                .orElseThrow(() -> new ResourceNotFoundException("nganhhoc.notFound", form.getMaNganh()));
        KhoaNhapHoc khoa = khoaNhapHocRepository.findById(form.getMaKhoaHoc())
                .orElseThrow(() -> new ResourceNotFoundException("khoanhaphoc.notFound", form.getMaKhoaHoc()));
        MonHoc mon = monHocRepository.findById(form.getMaMon())
                .orElseThrow(() -> new ResourceNotFoundException("monhoc.notFound", form.getMaMon()));

        ChuongTrinhDaoTao ctdt = new ChuongTrinhDaoTao(
                nganh, khoa, mon,
                form.getKhoiKienThuc(),
                form.getLoaiHocPhan(),
                form.getHocKyChuan()
        );
        chuongTrinhDaoTaoRepository.save(ctdt);
    }

    @Transactional
    public void updateCourse(CTDTForm form) {
        ChuongTrinhDaoTaoId id = new ChuongTrinhDaoTaoId(form.getMaNganh(), form.getMaKhoaHoc(), form.getMaMon());
        ChuongTrinhDaoTao ctdt = chuongTrinhDaoTaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ctdt.notFound", form.getMaMon()));
        ctdt.setKhoiKienThuc(form.getKhoiKienThuc());
        ctdt.setLoaiHocPhan(form.getLoaiHocPhan());
        ctdt.setHocKyChuan(form.getHocKyChuan());
        chuongTrinhDaoTaoRepository.save(ctdt);
    }

    @Transactional
    public int cloneCurriculum(String maNganh, String sourceKhoaHoc, String targetKhoaHoc) {
        if (sourceKhoaHoc != null && sourceKhoaHoc.equalsIgnoreCase(targetKhoaHoc)) {
            throw new BusinessRuleException("ctdt.clone.sameCohort");
        }
        NganhHoc nganh = nganhHocRepository.findById(maNganh)
                .orElseThrow(() -> new ResourceNotFoundException("nganhhoc.notFound", maNganh));
        KhoaNhapHoc targetKhoa = khoaNhapHocRepository.findById(targetKhoaHoc)
                .orElseThrow(() -> new ResourceNotFoundException("khoanhaphoc.notFound", targetKhoaHoc));

        List<ChuongTrinhDaoTao> sourceList = chuongTrinhDaoTaoRepository
                .findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc(maNganh, sourceKhoaHoc);
        if (sourceList.isEmpty()) {
            throw new BusinessRuleException("ctdt.clone.emptySource", sourceKhoaHoc);
        }

        Set<String> existingTargetMon = chuongTrinhDaoTaoRepository
                .findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHocOrderByHocKyChuanAscMonHoc_TenMonAsc(maNganh, targetKhoaHoc)
                .stream()
                .map(c -> c.getMonHoc().getMaMon())
                .collect(Collectors.toSet());

        List<ChuongTrinhDaoTao> toInsert = new ArrayList<>();
        for (ChuongTrinhDaoTao src : sourceList) {
            if (!existingTargetMon.contains(src.getMonHoc().getMaMon())) {
                toInsert.add(new ChuongTrinhDaoTao(
                        nganh, targetKhoa, src.getMonHoc(),
                        src.getKhoiKienThuc(), src.getLoaiHocPhan(), src.getHocKyChuan()
                ));
            }
        }
        if (!toInsert.isEmpty()) {
            chuongTrinhDaoTaoRepository.saveAll(toInsert);
        }

        List<DinhMucCTDT> sourceQuotas = dinhMucCTDTRepository
                .findByNganhHoc_MaNganhAndKhoaNhapHoc_MaKhoaHoc(maNganh, sourceKhoaHoc);
        for (DinhMucCTDT sq : sourceQuotas) {
            if (sq.getKhoiKienThuc() != null) {
                DinhMucCTDTId targetId = new DinhMucCTDTId(maNganh, targetKhoaHoc, sq.getKhoiKienThuc());
                if (!dinhMucCTDTRepository.existsById(targetId)) {
                    dinhMucCTDTRepository.save(new DinhMucCTDT(
                            nganh, targetKhoa, sq.getKhoiKienThuc(), sq.getTinChiTuChonToiThieu()));
                }
            }
        }
        return toInsert.size();
    }

    @Transactional
    public void removeCourse(String maNganh, String maKhoaHoc, String maMon) {
        ChuongTrinhDaoTaoId id = new ChuongTrinhDaoTaoId(maNganh, maKhoaHoc, maMon);
        if (chuongTrinhDaoTaoRepository.existsById(id)) {
            chuongTrinhDaoTaoRepository.deleteById(id);
        }
    }

    private CTDTRow toRow(ChuongTrinhDaoTao c) {
        return new CTDTRow(
                c.getNganhHoc().getMaNganh(),
                c.getNganhHoc().getTenNganh(),
                c.getKhoaNhapHoc().getMaKhoaHoc(),
                c.getMonHoc().getMaMon(),
                c.getMonHoc().getTenMon(),
                c.getMonHoc().getSoTinChi(),
                c.getKhoiKienThuc(),
                c.getLoaiHocPhan(),
                c.getHocKyChuan()
        );
    }
}
