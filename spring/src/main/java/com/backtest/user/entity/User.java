package com.backtest.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private byte[] passwordHash;

    @Column(name = "password_salt", nullable = false, length = 128)
    private byte[] passwordSalt;

    @Column(name = "password_algo", nullable = false, length = 50)
    private String passwordAlgorithm = "bcrypt";

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "investment_type", nullable = false, length = 20)
    private InvestmentType investmentType = InvestmentType.BALANCED;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("1")
    private boolean active = true;

    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected User() {
        // JPA only
    }

    public static User create(String username, String email, byte[] passwordHash, byte[] passwordSalt, InvestmentType investmentType) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.passwordSalt = passwordSalt;
        user.investmentType = investmentType == null ? InvestmentType.BALANCED : investmentType;
        user.passwordAlgorithm = "bcrypt";
        return user;
    }

    public void markLogin() {
        this.lastLoginAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void updateProfile(String profileImage, InvestmentType investmentType) {
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (investmentType != null) {
            this.investmentType = investmentType;
        }
    }

    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }

    public String issueEmailVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public String getPasswordAlgorithm() {
        return passwordAlgorithm;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public InvestmentType getInvestmentType() {
        return investmentType;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPassword(byte[] passwordHash, byte[] passwordSalt) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.passwordSalt = Objects.requireNonNull(passwordSalt, "passwordSalt must not be null");
    }
}
