package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoaiQuyetDinh implements DbLabeled {
    BAO_LUU("Bảo lưu"),
    THOI_HOC("Thôi học"),
    DINH_CHI("Đình chỉ"),
    TRO_LAI_HOC("Trở lại học");

    private final String label;
}
