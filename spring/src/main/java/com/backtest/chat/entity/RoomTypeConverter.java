package com.backtest.chat.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoomTypeConverter implements AttributeConverter<RoomType, String> {

    @Override
    public String convertToDatabaseColumn(RoomType attribute) {
        return attribute == null ? RoomType.PUBLIC.toDatabaseValue() : attribute.toDatabaseValue();
    }

    @Override
    public RoomType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return RoomType.PUBLIC;
        }
        return RoomType.from(dbData);
    }
}
