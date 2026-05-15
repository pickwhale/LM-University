package com.university.backend.ai.dto;

import java.util.List;

public record AiStreamDoneResponse(Long conversationId, Long messageId, List<AiSourceResponse> sources) {
}
