package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GioiTinh implements DbLabeled {
    NAM("Nam"),
    NU("Nữ"),
    KHAC("Khác");

    private final String label;
}
