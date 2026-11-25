package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiDangKy;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TrangThaiDangKyConverter extends LabeledEnumConverter<TrangThaiDangKy> {
    public TrangThaiDangKyConverter() {
        super(TrangThaiDangKy.class);
    }
}
