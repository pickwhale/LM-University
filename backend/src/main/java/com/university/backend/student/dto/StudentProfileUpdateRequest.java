package com.university.backend.student.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentProfileUpdateRequest(
    @NotBlank(message = "Full name is required") String fullName,
    String avatarPath,
    String gender,
    String college,
    String contactNumber
) {
}
