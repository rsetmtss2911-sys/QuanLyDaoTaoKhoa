package com.qldtkhoa.qldtkhoa_app.entity.enums;

import com.qldtkhoa.qldtkhoa_app.entity.converter.DbLabeled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrangThaiDangKy implements DbLabeled {
    HOP_LE("Hợp lệ"),
    DA_RUT_LOP("Đã rút lớp"),
    CAM_THI("Cấm thi");

    private final String label;
}
