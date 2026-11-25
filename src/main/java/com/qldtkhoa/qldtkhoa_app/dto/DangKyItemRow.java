package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;

public record DangKyItemRow(
        String maLHP,
        String maMon,
        String tenMon,
        Integer soTinChi,
        String tenGV,
        long siSoHienTai,
        Integer siSoToiDa,
        String lichHoc,
        TrangThaiLop trangThaiLop,
        boolean daDangKy,
        boolean daDangKyMonKhac
) {
}
