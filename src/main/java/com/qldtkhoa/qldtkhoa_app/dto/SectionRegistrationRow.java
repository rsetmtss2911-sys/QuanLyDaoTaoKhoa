package com.qldtkhoa.qldtkhoa_app.dto;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;

import java.time.LocalDateTime;

public record SectionRegistrationRow(
        String maSV,
        String hoTen,
        String maLopHC,
        LocalDateTime ngayDangKy,
        TrangThaiDangKy trangThaiDangKy
) {
}
