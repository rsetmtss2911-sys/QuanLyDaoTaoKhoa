package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;

import java.time.LocalDateTime;

public record SinhVienDangKyRow(
        String maLHP,
        String tenMon,
        Integer soTinChi,
        String maHK,
        LocalDateTime ngayDangKy,
        TrangThaiDangKy trangThaiDangKy
) {}
