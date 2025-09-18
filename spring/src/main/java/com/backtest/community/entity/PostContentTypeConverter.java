package com.backtest.community.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostContentTypeConverter implements AttributeConverter<PostContentType, String> {

    @Override
    public String convertToDatabaseColumn(PostContentType attribute) {
        return attribute == null ? PostContentType.MARKDOWN.toDatabaseValue() : attribute.toDatabaseValue();
    }

    @Override
    public PostContentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return PostContentType.MARKDOWN;
        }
        return PostContentType.valueOf(dbData.toUpperCase());
    }
}
