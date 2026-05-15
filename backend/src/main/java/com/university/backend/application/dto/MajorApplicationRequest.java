package com.university.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record MajorApplicationRequest(
    @NotNull(message = "Major id is required") Long majorId
) {
}
