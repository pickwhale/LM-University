package com.university.backend.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String issuer,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration,
    String secret
) {
}
