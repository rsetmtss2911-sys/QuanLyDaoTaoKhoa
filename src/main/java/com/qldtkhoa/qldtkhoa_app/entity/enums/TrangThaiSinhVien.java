package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrangThaiSinhVien implements DbLabeled {
    DANG_HOC("Đang học"),
    BAO_LUU("Bảo lưu"),
    DINH_CHI("Đình chỉ"),
    THOI_HOC("Thôi học"),
    DA_TOT_NGHIEP("Đã tốt nghiệp");

    private final String label;
}
