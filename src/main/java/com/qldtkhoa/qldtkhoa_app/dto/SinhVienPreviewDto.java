package com.qldtkhoa.qldtkhoa_app.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO xem trước thông tin sinh viên và các loại quyết định hợp lệ trên form QĐHV.
 */
public record SinhVienPreviewDto(
        boolean found,
        String maSV,
        String hoTen,
        String maLopHC,
        String trangThai,
        String trangThaiLabel,
        LocalDate latestDecisionDate,
        List<OptionDto> allowedTypes
) {
    public static SinhVienPreviewDto notFound(String maSV) {
        return new SinhVienPreviewDto(false, maSV, null, null, null, null, null, List.of());
    }

    public record OptionDto(String code, String label) {
    }
}
