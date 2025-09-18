package com.backtest.chat.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoomType {
    PUBLIC,
    PRIVATE,
    DIRECT;

    @JsonCreator
    public static RoomType from(String value) {
        if (value == null || value.isBlank()) {
            return PUBLIC;
        }
        return RoomType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
