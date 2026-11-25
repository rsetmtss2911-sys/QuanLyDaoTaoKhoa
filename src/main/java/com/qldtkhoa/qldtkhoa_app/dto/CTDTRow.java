package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;

public record CTDTRow(
        String maNganh,
        String tenNganh,
        String maKhoaHoc,
        String maMon,
        String tenMon,
        Integer soTinChi,
        KhoiKienThuc khoiKienThuc,
        LoaiHocPhan loaiHocPhan,
        Integer hocKyChuan
) {
}
