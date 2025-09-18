package com.backtest.auth.service;

import com.backtest.auth.dto.AuthResponse;
import com.backtest.auth.dto.AuthTokenResponse;
import com.backtest.auth.dto.LoginRequest;
import com.backtest.auth.dto.LogoutRequest;
import com.backtest.auth.dto.RefreshTokenRequest;
import com.backtest.auth.dto.RegisterRequest;
import com.backtest.auth.session.entity.UserSession;
import com.backtest.auth.session.repository.UserSessionRepository;
import com.backtest.global.exception.ApiException;
import com.backtest.global.security.JwtTokenProvider;
import com.backtest.user.entity.InvestmentType;
import com.backtest.user.entity.User;
import com.backtest.user.repository.UserRepository;
import com.backtest.user.service.UserService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       UserService userService,
                       UserSessionRepository sessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse register(RegisterRequest request, String userAgent, String ip) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "이미 사용 중인 닉네임입니다.");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String encoded = passwordEncoder.encode(request.password());
        User user = User.create(request.username(), request.email(), encoded.getBytes(StandardCharsets.UTF_8), salt,
                request.investmentType() == null ? InvestmentType.BALANCED : request.investmentType());
        user.markLogin();
        user = userRepository.save(user);

        AuthTokenResponse tokens = createSession(user, userAgent, ip);
        return new AuthResponse(userService.toSummary(user), tokens);
    }

    public AuthResponse login(LoginRequest request, String userAgent, String ip) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), new String(user.getPasswordHash(), StandardCharsets.UTF_8))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        user.markLogin();
        AuthTokenResponse tokens = createSession(user, userAgent, ip);
        return new AuthResponse(userService.toSummary(user), tokens);
    }

    public AuthTokenResponse refresh(RefreshTokenRequest request) {
        UserSession session = sessionRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "리프레시 토큰이 유효하지 않습니다."));

        if (session.isRevoked() || session.getRefreshExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "리프레시 토큰이 만료되었습니다.");
        }

        User user = session.getUser();
        AuthTokenResponse tokens = generateTokens(user);
        sessionRepository.save(UserSession.of(user, tokens.accessToken(), tokens.refreshToken(),
                tokens.accessTokenExpiresAt(), tokens.refreshTokenExpiresAt()));
        return tokens;
    }

    public void logout(Long userId, LogoutRequest request) {
        if (!StringUtils.hasText(request.refreshToken())) {
            return;
        }
        sessionRepository.findByRefreshToken(request.refreshToken()).ifPresent(session -> {
            if (!session.getUser().getId().equals(userId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "INVALID_SESSION_OWNER", "세션을 종료할 수 있는 권한이 없습니다.");
            }
            session.revoke();
            sessionRepository.save(session);
        });
    }

    private AuthTokenResponse createSession(User user, String userAgent, String ipAddress) {
        AuthTokenResponse tokens = generateTokens(user);
        UserSession session = UserSession.of(user, tokens.accessToken(), tokens.refreshToken(),
                tokens.accessTokenExpiresAt(), tokens.refreshTokenExpiresAt());
        session.setClientMetadata(StringUtils.hasText(userAgent) ? userAgent : null,
                StringUtils.hasText(ipAddress) ? ipAddress : null);
        session.touch();
        sessionRepository.save(session);
        return tokens;
    }

    private AuthTokenResponse generateTokens(User user) {
        Map<String, Object> claims = Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "admin", user.isAdmin()
        );
        String accessToken = tokenProvider.generateAccessToken(String.valueOf(user.getId()), claims);
        String refreshToken = tokenProvider.generateRefreshToken(String.valueOf(user.getId()));
        return new AuthTokenResponse(
                accessToken,
                tokenProvider.getExpiry(accessToken),
                refreshToken,
                tokenProvider.getExpiry(refreshToken)
        );
    }
}
