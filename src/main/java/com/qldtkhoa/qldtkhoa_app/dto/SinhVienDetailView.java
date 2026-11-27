package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;

import java.time.LocalDate;

public record SinhVienDetailView(
        String maSV,
        String hoTen,
        GioiTinh gioiTinh,
        LocalDate ngaySinh,
        TrangThaiSinhVien trangThaiHienTai,
        String maLopHC,
        String tenNganh,
        String maKhoaHoc,
        Integer namBatDau,
        String maCoVan,
        String tenCoVan,
        String email,
        Boolean taiKhoanKichHoat
) {
}
