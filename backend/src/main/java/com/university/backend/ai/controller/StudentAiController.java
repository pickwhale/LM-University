package com.university.backend.ai.controller;

import com.university.backend.ai.application.AiChatService;
import com.university.backend.ai.dto.AiChatStreamRequest;
import com.university.backend.ai.dto.AiConversationResponse;
import com.university.backend.ai.dto.AiMessageResponse;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/student/ai")
public class StudentAiController {

    private final AiChatService aiChatService;

    public StudentAiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(Authentication authentication, @Valid @RequestBody AiChatStreamRequest request) {
        return aiChatService.stream(requireAccount(authentication), request);
    }

    @GetMapping("/conversations")
    public ApiResponse<List<AiConversationResponse>> conversations(Authentication authentication) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(aiChatService.listConversations(account.accountId()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<AiMessageResponse>> messages(Authentication authentication, @PathVariable Long id) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(aiChatService.listMessages(account.accountId(), id));
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(Authentication authentication, @PathVariable Long id) {
        AuthenticatedAccount account = requireAccount(authentication);
        aiChatService.deleteConversation(account.accountId(), id);
        return ApiResponse.ok();
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
