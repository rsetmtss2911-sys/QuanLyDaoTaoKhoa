package com.qldtkhoa.qldtkhoa_app.entity.converter;

import jakarta.persistence.AttributeConverter;

/**
 * Converter cơ sở để chuyển đổi qua lại giữa Java Enum và nhãn NVARCHAR tiếng Việt trong SQL Server.
 */
public abstract class LabeledEnumConverter<E extends Enum<E> & DbLabeled> implements AttributeConverter<E, String> {

    private final Class<E> enumType;

    protected LabeledEnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        for (E constant : enumType.getEnumConstants()) {
            if (constant.getLabel().equalsIgnoreCase(dbData.trim())) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy giá trị enum " + enumType.getSimpleName() + " tương ứng với nhãn CSDL: " + dbData);
    }
}
