package com.backtest.auth.session.entity;

import com.backtest.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "access_token", nullable = false, length = 1024, unique = true)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, length = 512, unique = true)
    private String refreshToken;

    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType = "Bearer";

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", columnDefinition = "JSON")
    private String deviceInfo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "access_expires_at", nullable = false)
    private OffsetDateTime accessExpiresAt;

    @Column(name = "refresh_expires_at", nullable = false)
    private OffsetDateTime refreshExpiresAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    protected UserSession() {
        // JPA only
    }

    public static UserSession of(User user, String accessToken, String refreshToken,
                                 OffsetDateTime accessExpiresAt, OffsetDateTime refreshExpiresAt) {
        UserSession session = new UserSession();
        session.user = user;
        session.accessToken = accessToken;
        session.refreshToken = refreshToken;
        session.accessExpiresAt = accessExpiresAt;
        session.refreshExpiresAt = refreshExpiresAt;
        return session;
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void touch() {
        this.lastUsedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void setClientMetadata(String userAgent, String ipAddress) {
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public OffsetDateTime getAccessExpiresAt() {
        return accessExpiresAt;
    }

    public OffsetDateTime getRefreshExpiresAt() {
        return refreshExpiresAt;
    }

    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
