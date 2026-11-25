package com.qldtkhoa.qldtkhoa_app.dto;

public record MonHocRow(
        String maMon,
        String tenMon,
        Integer soTinChi,
        Integer tinChiLT,
        Integer tinChiTH,
        Double trongSoQT,
        Double trongSoGK,
        Double trongSoCK,
        String maBoMon,
        String tenBoMon,
        int soLuongTienQuyet
) {
}
