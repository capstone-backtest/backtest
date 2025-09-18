package com.backtest.chat.entity;

import com.backtest.chat.entity.ChatRoomMember.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ChatRoomMemberRoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role attribute) {
        return attribute == null ? Role.MEMBER.name().toLowerCase() : attribute.name().toLowerCase();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Role.MEMBER;
        }
        return Role.from(dbData);
    }
}
