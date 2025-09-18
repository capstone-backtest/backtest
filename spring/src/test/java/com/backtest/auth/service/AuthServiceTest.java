package com.backtest.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.backtest.auth.dto.AuthResponse;
import com.backtest.auth.dto.LoginRequest;
import com.backtest.auth.dto.LogoutRequest;
import com.backtest.auth.dto.RefreshTokenRequest;
import com.backtest.auth.dto.RegisterRequest;
import com.backtest.global.exception.ApiException;
import com.backtest.user.entity.InvestmentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerAndLoginLifecycle() {
        RegisterRequest register = new RegisterRequest("tester", "tester@example.com", "Password123", InvestmentType.BALANCED);
        AuthResponse registered = authService.register(register, "JUnit", "127.0.0.1");
        assertThat(registered.user().email()).isEqualTo("tester@example.com");
        assertThat(registered.tokens().accessToken()).isNotBlank();

        assertThatThrownBy(() -> authService.register(register, "JUnit", "127.0.0.1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");

        AuthResponse login = authService.login(new LoginRequest("tester@example.com", "Password123"), "JUnit", "127.0.0.1");
        assertThat(login.tokens().accessToken()).isNotBlank();
        assertThat(login.tokens().refreshToken()).isNotBlank();

        RefreshTokenRequest refresh = new RefreshTokenRequest(login.tokens().refreshToken());
        var refreshed = authService.refresh(refresh);
        assertThat(refreshed.accessToken()).isNotEqualTo(login.tokens().accessToken());

        authService.logout(login.user().id(), new LogoutRequest(login.tokens().refreshToken()));
    }
}
