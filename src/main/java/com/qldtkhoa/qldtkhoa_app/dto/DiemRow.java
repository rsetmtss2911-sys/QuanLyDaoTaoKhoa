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
        boolean camThi,
        String lyDoCamThi
) {
    public DiemRow(
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
        this(maSV, hoTen, maLopHC, trangThaiDangKy, diemQuaTrinh, diemGiuaKy, diemCuoiKy,
                diemTong, diemHe4, diemChu, camThi, null);
    }
}
