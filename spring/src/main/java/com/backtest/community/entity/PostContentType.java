package com.backtest.community.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostContentType {
    TEXT,
    MARKDOWN;

    @JsonCreator
    public static PostContentType from(String value) {
        if (value == null || value.isBlank()) {
            return MARKDOWN;
        }
        return PostContentType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
