package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;

public record LopHocPhanDetailView(
        String maLHP,
        String maMon,
        String tenMon,
        Integer soTinChi,
        Integer tinChiLT,
        Integer tinChiTH,
        Double trongSoQT,
        Double trongSoGK,
        Double trongSoCK,
        String maHK,
        String tenHocKy,
        Integer namHoc,
        String maGV,
        String tenGV,
        Integer siSoToiDa,
        TrangThaiLop trangThaiLop
) {
}
