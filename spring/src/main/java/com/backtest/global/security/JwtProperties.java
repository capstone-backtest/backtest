package com.backtest.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret,
                            @DefaultValue("3600000") long accessTokenExpiration,
                            @DefaultValue("604800000") long refreshTokenExpiration) {
}
