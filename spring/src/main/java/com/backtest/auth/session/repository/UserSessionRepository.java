package com.backtest.auth.session.repository;

import com.backtest.auth.session.entity.UserSession;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshToken(String refreshToken);
    Optional<UserSession> findByAccessToken(String accessToken);
    long deleteByRefreshExpiresAtBefore(OffsetDateTime cutoff);
}
