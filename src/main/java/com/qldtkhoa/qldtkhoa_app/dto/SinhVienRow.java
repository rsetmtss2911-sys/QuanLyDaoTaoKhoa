package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;

import java.time.LocalDate;

public record SinhVienRow(
        String maSV,
        String hoTen,
        String email,
        GioiTinh gioiTinh,
        LocalDate ngaySinh,
        String maLopHC,
        String tenLopHC,
        TrangThaiSinhVien trangThaiHienTai
) {
}
