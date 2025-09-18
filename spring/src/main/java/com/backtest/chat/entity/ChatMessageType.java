package com.backtest.chat.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatMessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM;

    @JsonCreator
    public static ChatMessageType from(String value) {
        if (value == null || value.isBlank()) {
            return TEXT;
        }
        return ChatMessageType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
