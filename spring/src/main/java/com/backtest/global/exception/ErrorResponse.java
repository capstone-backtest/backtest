package com.backtest.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, OffsetDateTime timestamp) {
    public static ErrorResponse from(ApiException ex) {
        return new ErrorResponse(ex.getCode(), ex.getMessage(), OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
