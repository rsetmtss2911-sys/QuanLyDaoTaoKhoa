package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiLop;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TrangThaiLopConverter extends LabeledEnumConverter<TrangThaiLop> {
    public TrangThaiLopConverter() {
        super(TrangThaiLop.class);
    }
}
