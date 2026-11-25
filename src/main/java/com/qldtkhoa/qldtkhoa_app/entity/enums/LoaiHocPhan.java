package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoaiHocPhan implements DbLabeled {
    BAT_BUOC("Bắt buộc"),
    TU_CHON("Tự chọn");

    private final String label;
}
