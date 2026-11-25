package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.KhoiKienThuc;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class KhoiKienThucConverter extends LabeledEnumConverter<KhoiKienThuc> {
    public KhoiKienThucConverter() {
        super(KhoiKienThuc.class);
    }
}
