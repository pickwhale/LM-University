package com.university.backend.application.dto;

import jakarta.validation.constraints.NotNull;

public record UniversityApplicationRequest(
    @NotNull(message = "University id is required") Long universityId
) {
}
