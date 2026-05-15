package com.university.backend.ai.dto;

import java.time.LocalDateTime;

public record AiMessageResponse(Long id, Long conversationId, String role, String content, String sourcesJson, LocalDateTime createdAt) {
}
