package com.backtest.chat.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ChatMessageTypeConverter implements AttributeConverter<ChatMessageType, String> {

    @Override
    public String convertToDatabaseColumn(ChatMessageType attribute) {
        return attribute == null ? ChatMessageType.TEXT.toDatabaseValue() : attribute.toDatabaseValue();
    }

    @Override
    public ChatMessageType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return ChatMessageType.TEXT;
        }
        return ChatMessageType.from(dbData);
    }
}
