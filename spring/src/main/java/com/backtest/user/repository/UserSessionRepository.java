package com.backtest.repository;

import com.backtest.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 세션 Repository
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * 토큰으로 세션 찾기
     */
    Optional<UserSession> findByToken(String token);

    /**
     * 리프레시 토큰으로 세션 찾기
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);

    /**
     * 사용자의 활성 세션 조회
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 특정 디바이스 세션 조회
     */
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.deviceId = :deviceId AND s.isActive = true")
    Optional<UserSession> findActiveSessionByUserIdAndDeviceId(
        @Param("userId") Long userId, 
        @Param("deviceId") String deviceId
    );

    /**
     * 만료된 세션 조회
     */
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * 사용자의 다른 세션들 비활성화
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false, s.updatedAt = :now " +
           "WHERE s.user.id = :userId AND s.id != :excludeSessionId AND s.isActive = true")
    void deactivateOtherSessions(
        @Param("userId") Long userId, 
        @Param("excludeSessionId") Long excludeSessionId, 
        @Param("now") LocalDateTime now
    );

    /**
     * 만료된 세션 삭제
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :expirationTime")
    int deleteExpiredSessions(@Param("expirationTime") LocalDateTime expirationTime);

    /**
     * 사용자의 모든 세션 비활성화
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false, s.updatedAt = :now " +
           "WHERE s.user.id = :userId AND s.isActive = true")
    void deactivateAllUserSessions(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}