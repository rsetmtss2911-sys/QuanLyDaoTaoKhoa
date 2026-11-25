package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.GioiTinh;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GioiTinhConverter extends LabeledEnumConverter<GioiTinh> {
    public GioiTinhConverter() {
        super(GioiTinh.class);
    }
}
