package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;

public record LopHocPhanRow(
        String maLHP,
        String maMon,
        String tenMon,
        Integer soTinChi,
        String maGV,
        String tenGV,
        String maHK,
        Integer siSoToiDa,
        long siSoHienTai,
        TrangThaiLop trangThaiLop
) {
}
