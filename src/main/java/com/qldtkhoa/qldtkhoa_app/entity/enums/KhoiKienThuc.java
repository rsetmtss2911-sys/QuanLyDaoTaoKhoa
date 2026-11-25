package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KhoiKienThuc implements DbLabeled {
    DAI_CUONG("Đại cương"),
    CO_SO_NGANH("Cơ sở ngành"),
    CHUYEN_NGANH("Chuyên ngành");

    private final String label;
}
