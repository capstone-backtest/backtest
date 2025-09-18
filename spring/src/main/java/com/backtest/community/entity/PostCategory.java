package com.backtest.community.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostCategory {
    GENERAL,
    STRATEGY,
    QUESTION,
    NEWS,
    BACKTEST_SHARE;

    @JsonCreator
    public static PostCategory from(String value) {
        if (value == null || value.isBlank()) {
            return GENERAL;
        }
        return PostCategory.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toDatabaseValue() {
        return name().toLowerCase();
    }
}
