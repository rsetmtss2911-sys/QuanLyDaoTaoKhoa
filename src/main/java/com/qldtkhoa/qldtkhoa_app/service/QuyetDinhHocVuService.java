package com.qldtkhoa.qldtkhoa_app.service;

import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhForm;
import com.qldtkhoa.qldtkhoa_app.dto.QuyetDinhRow;
import com.qldtkhoa.qldtkhoa_app.entity.QuyetDinhHocVu;
import com.qldtkhoa.qldtkhoa_app.entity.SinhVien;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import com.qldtkhoa.qldtkhoa_app.exception.BusinessRuleException;
import com.qldtkhoa.qldtkhoa_app.exception.DuplicateResourceException;
import com.qldtkhoa.qldtkhoa_app.exception.ResourceNotFoundException;
import com.qldtkhoa.qldtkhoa_app.repository.QuyetDinhHocVuRepository;
import com.qldtkhoa.qldtkhoa_app.repository.SinhVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuyetDinhHocVuService {

    private final QuyetDinhHocVuRepository quyetDinhHocVuRepository;
    private final SinhVienRepository sinhVienRepository;

    public Page<QuyetDinhRow> search(String q, String maSV, Pageable pageable) {
        return quyetDinhHocVuRepository.search(q == null ? "" : q.trim(), maSV, pageable)
                .map(this::toRow);
    }

    @Transactional
    public void create(QuyetDinhForm form) {
        String ma = form.getMaQuyetDinh().trim();
        if (quyetDinhHocVuRepository.existsById(ma)) {
            throw new DuplicateResourceException("quyetdinh.maQuyetDinh.duplicate", ma);
        }

        SinhVien sv = sinhVienRepository.findById(form.getMaSV().trim())
                .orElseThrow(() -> new ResourceNotFoundException("sinhvien.notFound", form.getMaSV()));

        // Máy trạng thái chuyển đổi học vụ (Academic Decision State Machine)
        LoaiQuyetDinh lqd = form.getLoaiQuyetDinh();
        switch (lqd) {
            case BAO_LUU -> sv.setTrangThaiHienTai(TrangThaiSinhVien.BAO_LUU);
            case DINH_CHI -> sv.setTrangThaiHienTai(TrangThaiSinhVien.DINH_CHI);
            case THOI_HOC -> sv.setTrangThaiHienTai(TrangThaiSinhVien.THOI_HOC);
            case TRO_LAI_HOC -> sv.setTrangThaiHienTai(TrangThaiSinhVien.DANG_HOC);
        }
        sinhVienRepository.save(sv);

        QuyetDinhHocVu qd = new QuyetDinhHocVu(
                ma,
                sv,
                lqd,
                form.getNgayHieuLuc(),
                form.getLyDo() != null ? form.getLyDo().trim() : null
        );
        quyetDinhHocVuRepository.save(qd);
    }

    public java.util.List<QuyetDinhRow> findBySinhVien(String maSV) {
        return quyetDinhHocVuRepository.findBySinhVien_MaSVOrderByNgayHieuLucDesc(maSV).stream()
                .map(this::toRow)
                .toList();
    }

    @Transactional
    public void delete(String maQuyetDinh) {
        // Trigger SQL TRG_QuyetDinhHocVu_PreventDelete chặn xóa quyết định (Tính bất biến)
        throw new BusinessRuleException("decision.error.immutable", "Quyết định học vụ có tính bất biến pháp lý và không được phép xóa!");
    }

    private QuyetDinhRow toRow(QuyetDinhHocVu qd) {
        return new QuyetDinhRow(
                qd.getMaQuyetDinh(),
                qd.getSinhVien().getMaSV(),
                qd.getSinhVien().getHoTen(),
                qd.getSinhVien().getLopHanhChinh().getMaLopHC(),
                qd.getLoaiQuyetDinh(),
                qd.getNgayHieuLuc(),
                qd.getLyDo()
        );
    }
}
