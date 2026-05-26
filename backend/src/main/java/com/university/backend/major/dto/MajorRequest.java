package com.university.backend.major.dto;

import jakarta.validation.constraints.*;

public record MajorRequest(
    @NotBlank(message = "Major code is required")
    @Pattern(regexp = "^[A-Z-]{1,10}$", message = "Major code must consist of uppercase letters and dashes, max 10 characters")
    String code,
    @NotNull(message = "University is required") Long universityId,
    @NotBlank(message = "Major name is required")
    @Size(max = 50, message = "Major name must not exceed 50 characters")
    String name,
    String coverPath,
    @Pattern(regexp = "^\\d+ years$", message = "Duration of study must be digits followed by a space and 'years', e.g., '4 years'")
    String durationOfStudy,
    @Pattern(regexp = "^(0|[1-9]\\d{0,2}|1000)$", message = "Cut-off score must be a number between 0 and 1000")
    String cutOffScore,
    @Positive(message = "Enrollment quota must be a positive integer")
    Integer enrollmentQuota,
    @Size(max = 100, message = "Curriculum must not exceed 100 characters")
    String curriculum
) {
}
