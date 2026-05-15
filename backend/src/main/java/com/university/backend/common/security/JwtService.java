package com.university.backend.common.security;

import com.university.backend.account.domain.Role;
import com.university.backend.common.error.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;
    private final Environment environment;

    public JwtService(JwtProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @PostConstruct
    void validateSecret() {
        String secret = properties.secret();
        boolean localProfile = Arrays.asList(environment.getActiveProfiles()).contains("local");
        if (!localProfile && (secret == null || secret.length() < 32 || "change-me-change-me-change-me-change-me".equals(secret))) {
            throw new IllegalStateException("APP_JWT_SECRET must be set to a unique secret with at least 32 characters");
        }
    }

    public String generateAccessToken(AuthenticatedAccount account) {
        return buildToken(account, properties.accessTokenExpiration(), "access");
    }

    public String generateRefreshToken(AuthenticatedAccount account) {
        return buildToken(account, properties.refreshTokenExpiration(), "refresh");
    }

    public AuthenticatedAccount parseRefreshToken(String token) {
        return parseToken(token, "refresh");
    }

    private String buildToken(AuthenticatedAccount account, java.time.Duration expiration, String tokenType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expiration);
        return Jwts.builder()
            .issuer(properties.issuer())
            .subject(account.username())
            .claim("accountId", account.accountId())
            .claim("role", account.role().name())
            .claim("tokenType", tokenType)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact();
    }

    public AuthenticatedAccount parseAccessToken(String token) {
        return parseToken(token, "access");
    }

    private AuthenticatedAccount parseToken(String token, String expectedTokenType) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            String tokenType = claims.get("tokenType", String.class);
            if (tokenType != null && !expectedTokenType.equals(tokenType)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token type");
            }
            Long accountId = claims.get("accountId", Long.class);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            return new AuthenticatedAccount(accountId, username, Role.valueOf(role));
        } catch (JwtException | IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
    }
}
