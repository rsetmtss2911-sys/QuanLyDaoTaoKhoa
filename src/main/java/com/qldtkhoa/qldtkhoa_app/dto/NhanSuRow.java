package com.qldtkhoa.qldtkhoa_app.dto;

public record NhanSuRow(
        String maNS,
        String hoTen,
        String email,
        String maVaiTro,
        String tenVaiTro,
        Boolean trangThaiKichHoat,
        boolean isGiangVien,
        String hocHam,
        String hocVi,
        String tenBoMon
) {}
