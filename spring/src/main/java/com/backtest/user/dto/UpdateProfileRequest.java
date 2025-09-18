package com.backtest.user.dto;

public class UpdateProfileRequest {
    private String username;
    private String investmentType;

    public UpdateProfileRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getInvestmentType() { return investmentType; }
    public void setInvestmentType(String investmentType) { this.investmentType = investmentType; }
}
package com.backtest.user.dto;

import jakarta.validation.constraints.Size;

/**
 * 사용자 프로필 업데이트 요청 DTO
 */
public class UpdateProfileRequest {

    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String fullName;

    @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다")
    private String profileImageUrl;

    // 기본 생성자
    public UpdateProfileRequest() {}

    // 생성자
    public UpdateProfileRequest(String fullName, String profileImageUrl) {
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}