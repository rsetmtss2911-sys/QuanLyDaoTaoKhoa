package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiHocPhan;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoaiHocPhanConverter extends LabeledEnumConverter<LoaiHocPhan> {
    public LoaiHocPhanConverter() {
        super(LoaiHocPhan.class);
    }
}
