package com.university.backend.interaction.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultationReplyRequest(@NotBlank(message = "Reply is required") String reply) {
}
