package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;

public record DiemRow(
        String maSV,
        String hoTen,
        String maLopHC,
        TrangThaiDangKy trangThaiDangKy,
        Double diemQuaTrinh,
        Double diemGiuaKy,
        Double diemCuoiKy,
        Double diemTong,
        Double diemHe4,
        String diemChu,
        boolean camThi
) {
}
