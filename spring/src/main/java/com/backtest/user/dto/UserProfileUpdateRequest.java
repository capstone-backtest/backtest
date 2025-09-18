package com.backtest.user.dto;

import com.backtest.user.entity.InvestmentType;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 500, message = "프로필 이미지는 500자 이하로 입력해주세요.")
        String profileImage,
        InvestmentType investmentType
) {
}
