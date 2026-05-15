package com.university.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatStreamRequest(Long conversationId, @NotBlank String message) {
}
