package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrangThaiLop implements DbLabeled {
    CHO_MO("Chờ mở"),
    DA_CHOT_DANH_SACH("Đã chốt danh sách"),
    DA_HUY("Đã hủy");

    private final String label;
}
