package com.university.backend.auth.dto;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    MeResponse account
) {
}
