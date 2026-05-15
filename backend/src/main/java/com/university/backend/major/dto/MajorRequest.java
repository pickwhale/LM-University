package com.university.backend.major.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MajorRequest(
    @NotBlank(message = "Major code is required") String code,
    @NotNull(message = "University is required") Long universityId,
    @NotBlank(message = "Major name is required") String name,
    String coverPath,
    String durationOfStudy,
    String cutOffScore,
    Integer enrollmentQuota,
    String curriculum
) {
}
