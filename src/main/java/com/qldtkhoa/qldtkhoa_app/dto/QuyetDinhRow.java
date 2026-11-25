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
        String lyDo
) {
}
