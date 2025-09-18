package com.backtest.community.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PostCategoryConverter implements AttributeConverter<PostCategory, String> {

    @Override
    public String convertToDatabaseColumn(PostCategory attribute) {
        return attribute == null ? PostCategory.GENERAL.toDatabaseValue() : attribute.toDatabaseValue();
    }

    @Override
    public PostCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return PostCategory.GENERAL;
        }
        return PostCategory.valueOf(dbData.toUpperCase());
    }
}
