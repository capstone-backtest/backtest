package com.backtest.user.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 새로고침 요청 DTO
 */
public class RefreshTokenRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;

    // 기본 생성자
    public RefreshTokenRequest() {}

    // 생성자
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}