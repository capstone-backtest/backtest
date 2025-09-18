package com.backtest.user.dto;

import com.backtest.user.entity.InvestmentType;
import java.time.OffsetDateTime;

public record UserSummary(Long id,
                          String username,
                          String email,
                          String profileImage,
                          InvestmentType investmentType,
                          boolean admin,
                          OffsetDateTime createdAt,
                          OffsetDateTime lastLoginAt) {
}
