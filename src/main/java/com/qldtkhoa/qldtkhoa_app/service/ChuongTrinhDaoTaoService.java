package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.config.AcademicPolicy;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTForm;
import com.qldtkhoa.qldtkhoa_app.dto.CTDTRow;
import com.qldtkhoa.qldtkhoa_app.entity.ChuongTrinhDaoTao;
import com.qldtkhoa.qldtkhoa_app.entity.KhoaNhapHoc;
import com.qldtkhoa.qldtkhoa_app.entity.MonHoc;
import com.qldtkhoa.qldtkhoa_app.entity.NganhHoc;
import com.qldtkhoa.qldtkhoa_app.entity.id.ChuongTrinhDaoTaoId;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.ChuongTrinhDaoTaoRepository;
import com.qldtkhoa.qldtkhoa_app.repository.KhoaNhapHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.MonHocRepository;
import com.qldtkhoa.qldtkhoa_app.repository.NganhHocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qldtkhoa.qldtkhoa_app.dto.TienDoHocTapView;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.view.BangDiemSinhVienView;
import com.qldtkhoa.qldtkhoa_app.repository.BangDiemSinhVienViewRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChuongTrinhDaoTaoService {

    private final ChuongTrinhDaoTaoRepository chuongTrinhDaoTaoRepository;
    private final NganhHocRepository nganhHocRepository;
    private final KhoaNhapHocRepository khoaNhapHocRepository;
    private final MonHocRepository monHocRepository;
    private final SinhVienRepository sinhVienRepository;
    private final BangDiemSinhVienViewRepository bangDiemViewRepository;
    private final AcademicPolicy academicPolicy;

    public TienDoHocTapView getTienDoCTDT(String maSV) {
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", maSV));

        String maNganh = sv.getLopHanhChinh().getNganhHoc().getMaNganh();
        String tenNganh = sv.getLopHanhChinh().getNganhHoc().getTenNganh();
        String maKhoaHoc = sv.getLopHanhChinh().getKhoaNhapHoc().getMaKhoaHoc();

        List<CTDTRow> curriculum = findByNganhAndKhoa(maNganh, maKhoaHoc);
        List<BangDiemSinhVienView> grades = bangDiemViewRepository.findById_MaSVOrderByNamHocDescMaHKAscTenMonAsc(maSV);

        double passScore = academicPolicy.passScore();
        Set<String> passedSubjects = grades.stream()
                .filter(g -> g.getDiemTong() != null && g.getDiemTong() >= passScore)
                .map(g -> g.getMaMon())
                .collect(Collectors.toSet());

        int totalCurriculumCredits = 0;
        int completedCredits = 0;
        for (CTDTRow c : curriculum) {
            int tc = c.soTinChi() != null ? c.soTinChi() : 0;
            totalCurriculumCredits += tc;
            if (passedSubjects.contains(c.maMon())) {
                completedCredits += tc;
            }
        }

        int progressPercent = totalCurriculumCredits > 0 ? (completedCredits * 100 / totalCurriculumCredits) : 0;

        return new TienDoHocTapView(
                sv.getMaSV(),
                sv.getHoTen(),
                tenNganh,
                maKhoaHoc,
                totalCurriculumCredits,
                completedCredits,
                progressPercent,
                curriculum,
                passedSubjects
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

    @Transactional
    public void addCourse(CTDTForm form) {
        ChuongTrinhDaoTaoId id = new ChuongTrinhDaoTaoId(form.getMaNganh(), form.getMaKhoaHoc(), form.getMaMon());
        if (chuongTrinhDaoTaoRepository.existsById(id)) {
            throw new DuplicateResourceException("ctdt.duplicate", "Môn học này đã có trong chương trình đào tạo!");
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
