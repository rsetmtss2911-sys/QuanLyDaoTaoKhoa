package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;

import java.time.LocalDate;

public record QuyetDinhRow(
        String maQuyetDinh,
        String maSV,
        String hoTenSV,
        String maLopHC,
        LoaiQuyetDinh loaiQuyetDinh,
        LocalDate ngayHieuLuc,
        String lyDo,
        boolean pendingEffective
) {
    public QuyetDinhRow(
            String maQuyetDinh,
            String maSV,
            String hoTenSV,
            String maLopHC,
            LoaiQuyetDinh loaiQuyetDinh,
            LocalDate ngayHieuLuc,
            String lyDo
    ) {
        this(maQuyetDinh, maSV, hoTenSV, maLopHC, loaiQuyetDinh, ngayHieuLuc, lyDo, false);
    }
}
