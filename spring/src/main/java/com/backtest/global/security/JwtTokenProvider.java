package com.backtest.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import javax.crypto.SecretKey;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(resolveSecret(properties.secret()));
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, properties.accessTokenExpiration());
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of("type", "refresh"), properties.refreshTokenExpiration());
    }

    private String buildToken(String subject, Map<String, Object> claims, long validityMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public OffsetDateTime getExpiry(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.toInstant().atOffset(ZoneOffset.UTC);
    }

    private static byte[] resolveSecret(String rawSecret) {
        if (rawSecret == null || rawSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        try {
            return Decoders.BASE64.decode(rawSecret);
        } catch (IllegalArgumentException ignored) {
            return hexToBytes(rawSecret);
        }
    }

    private static byte[] hexToBytes(String value) {
        String normalized = value.replace(" ", "").replace("0x", "").toLowerCase(Locale.US);
        if (normalized.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length");
        }
        byte[] bytes = new byte[normalized.length() / 2];
        for (int i = 0; i < normalized.length(); i += 2) {
            int high = Character.digit(normalized.charAt(i), 16);
            int low = Character.digit(normalized.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid hex character");
            }
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        return bytes;
    }
}
