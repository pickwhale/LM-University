package com.university.backend.auth.dto;

public record MeResponse(
    Long accountId,
    String username,
    String role,
    String displayName,
    Long studentProfileId,
    String studentNo,
    String fullName,
    String college,
    String contactNumber
) {
}
