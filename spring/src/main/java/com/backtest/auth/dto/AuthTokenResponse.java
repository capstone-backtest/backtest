package com.backtest.auth.dto;

import java.time.OffsetDateTime;

public record AuthTokenResponse(String accessToken,
                                OffsetDateTime accessTokenExpiresAt,
                                String refreshToken,
                                OffsetDateTime refreshTokenExpiresAt) {
}
