package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.LoaiQuyetDinh;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LoaiQuyetDinhConverter extends LabeledEnumConverter<LoaiQuyetDinh> {
    public LoaiQuyetDinhConverter() {
        super(LoaiQuyetDinh.class);
    }
}
