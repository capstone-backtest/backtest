package com.backtest.domain.user.dto;

import com.backtest.entity.User;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
public class UserResponse {

    private Long id;
    
    private String email;
    
    private String username;
    
    private String fullName;
    
    private String profileImageUrl;
    
    private User.Role role;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastLoginAt;

    // 기본 생성자
    public UserResponse() {}

    // 생성자
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.profileImageUrl = user.getProfileImageUrl();
        this.role = user.getRole();
        this.isActive = user.getIsActive();
        this.createdAt = user.getCreatedAt();
        this.lastLoginAt = user.getLastLoginAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}