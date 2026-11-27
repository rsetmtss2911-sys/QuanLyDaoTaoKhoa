package com.qldtkhoa.qldtkhoa_app.dto;

public record MonHocDetailView(
        String maMon,
        String tenMon,
        Integer soTinChi,
        Integer tinChiLT,
        Integer tinChiTH,
        Double trongSoQT,
        Double trongSoGK,
        Double trongSoCK,
        Boolean tinhVaoGPA,
        String tenBoMon,
        String tenKhoa
) {
}
