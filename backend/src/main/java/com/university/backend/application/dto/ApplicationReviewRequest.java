package com.university.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationReviewRequest(
    @NotBlank(message = "Status is required") String status,
    String reviewComment
) {
}
