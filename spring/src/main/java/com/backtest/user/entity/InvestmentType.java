package com.backtest.user.entity;

public enum InvestmentType {
    CONSERVATIVE,
    MODERATE,
    BALANCED,
    AGGRESSIVE,
    SPECULATIVE;

    public static InvestmentType fromDatabaseValue(String value) {
        return value == null ? BALANCED : InvestmentType.valueOf(value.toUpperCase());
    }
}
