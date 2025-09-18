package com.backtest.auth.dto;

import com.backtest.user.dto.UserSummary;

public record AuthResponse(UserSummary user, AuthTokenResponse tokens) {
}
