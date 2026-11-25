package com.qldtkhoa.qldtkhoa_app.entity.converter;

import com.qldtkhoa.qldtkhoa_app.entity.enums.TrangThaiSinhVien;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TrangThaiSinhVienConverter extends LabeledEnumConverter<TrangThaiSinhVien> {
    public TrangThaiSinhVienConverter() {
        super(TrangThaiSinhVien.class);
    }
}
