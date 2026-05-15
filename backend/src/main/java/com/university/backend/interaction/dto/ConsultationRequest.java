package com.university.backend.interaction.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultationRequest(@NotBlank(message = "Question is required") String question) {
}
