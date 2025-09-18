package com.backtest.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 등록 요청 DTO
 */
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    private String password;

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 30, message = "사용자명은 3자 이상 30자 이하여야 합니다")
    private String username;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String fullName;

    // 기본 생성자
    public SignupRequest() {}

    // 생성자
    public SignupRequest(String email, String password, String username, String fullName) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.fullName = fullName;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}