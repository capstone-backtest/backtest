package com.backtest.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InvestmentTypeConverter implements AttributeConverter<InvestmentType, String> {

    @Override
    public String convertToDatabaseColumn(InvestmentType attribute) {
        return attribute == null ? InvestmentType.BALANCED.name().toLowerCase() : attribute.name().toLowerCase();
    }

    @Override
    public InvestmentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return InvestmentType.BALANCED;
        }
        return InvestmentType.valueOf(dbData.toUpperCase());
    }
}
