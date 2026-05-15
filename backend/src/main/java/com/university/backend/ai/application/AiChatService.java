package com.university.backend.ai.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.backend.ai.domain.AiChatConversation;
import com.university.backend.ai.domain.AiChatMessage;
import com.university.backend.ai.dto.AiChatStreamRequest;
import com.university.backend.ai.dto.AiConversationResponse;
import com.university.backend.ai.dto.AiMessageResponse;
import com.university.backend.ai.dto.AiStreamDoneResponse;
import com.university.backend.ai.infrastructure.AiChatConversationMapper;
import com.university.backend.ai.infrastructure.AiChatMessageMapper;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AiChatService {

    private final AiChatConversationMapper conversationMapper;
    private final AiChatMessageMapper messageMapper;
    private final StudentProfileService studentProfileService;
    private final AiConfigService aiConfigService;
    private final AiContextService aiContextService;
    private final GenericAiClient genericAiClient;
    private final ObjectMapper objectMapper;

    public AiChatService(
        AiChatConversationMapper conversationMapper,
        AiChatMessageMapper messageMapper,
        StudentProfileService studentProfileService,
        AiConfigService aiConfigService,
        AiContextService aiContextService,
        GenericAiClient genericAiClient,
        ObjectMapper objectMapper
    ) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.studentProfileService = studentProfileService;
        this.aiConfigService = aiConfigService;
        this.aiContextService = aiContextService;
        this.genericAiClient = genericAiClient;
        this.objectMapper = objectMapper;
    }

    public SseEmitter stream(AuthenticatedAccount account, AiChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> streamInternal(account, request, emitter));
        return emitter;
    }

    public List<AiConversationResponse> listConversations(Long accountId) {
        StudentProfile student = requireStudent(accountId);
        return conversationMapper.selectList(
            new LambdaQueryWrapper<AiChatConversation>()
                .eq(AiChatConversation::getStudentId, student.getId())
                .orderByDesc(AiChatConversation::getUpdatedAt)
                .orderByDesc(AiChatConversation::getId)
        ).stream().map(this::mapConversation).toList();
    }

    public List<AiMessageResponse> listMessages(Long accountId, Long conversationId) {
        requireOwnConversation(accountId, conversationId);
        return messageMapper.selectList(
            new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .orderByAsc(AiChatMessage::getCreatedAt)
                .orderByAsc(AiChatMessage::getId)
        ).stream().map(this::mapMessage).toList();
    }

    public void deleteConversation(Long accountId, Long conversationId) {
        requireOwnConversation(accountId, conversationId);
        messageMapper.delete(new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getConversationId, conversationId));
        conversationMapper.deleteById(conversationId);
    }

    private void streamInternal(AuthenticatedAccount account, AiChatStreamRequest request, SseEmitter emitter) {
        try {
            if (!StringUtils.hasText(request.message())) {
                sendError(emitter, "请输入问题");
                return;
            }
            AiProviderConfig config = aiConfigService.getConfig();
            if (!config.enabled()) {
                sendError(emitter, "AI 功能尚未启用，请联系管理员配置。");
                return;
            }
            StudentProfile student = requireStudent(account.accountId());
            AiChatConversation conversation = request.conversationId() == null
                ? createConversation(student.getId(), request.message())
                : requireOwnConversation(account.accountId(), request.conversationId());

            insertMessage(conversation.getId(), "user", request.message(), null);
            AiContextPackage context = aiContextService.build(account.accountId(), request.message());
            String historyJson = buildHistoryJson(conversation.getId());
            String sourcesJson = objectMapper.writeValueAsString(context.sources());
            StringBuilder answer = new StringBuilder();
            genericAiClient.stream(config, request.message(), context.contextText(), historyJson, delta -> {
                answer.append(delta);
                sendDelta(emitter, delta);
            });

            AiChatMessage assistantMessage = insertMessage(conversation.getId(), "assistant", answer.toString(), sourcesJson);
            touchConversation(conversation);
            sendDone(emitter, new AiStreamDoneResponse(conversation.getId(), assistantMessage.getId(), context.sources()));
        } catch (Exception exception) {
            sendError(emitter, exception.getMessage() == null ? "AI 对话失败" : exception.getMessage());
        }
    }

    private AiChatConversation createConversation(Long studentId, String message) {
        LocalDateTime now = LocalDateTime.now();
        AiChatConversation conversation = new AiChatConversation();
        conversation.setStudentId(studentId);
        conversation.setTitle(titleFrom(message));
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);
        conversationMapper.insert(conversation);
        return conversation;
    }

    private AiChatMessage insertMessage(Long conversationId, String role, String content, String sourcesJson) {
        AiChatMessage message = new AiChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setSourcesJson(sourcesJson);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }

    private void touchConversation(AiChatConversation conversation) {
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);
    }

    private String buildHistoryJson(Long conversationId) throws Exception {
        List<AiChatMessage> latest = messageMapper.selectList(
            new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .orderByDesc(AiChatMessage::getCreatedAt)
                .orderByDesc(AiChatMessage::getId)
                .last("limit 12")
        );
        List<Map<String, String>> history = new ArrayList<>();
        for (int i = latest.size() - 1; i >= 0; i--) {
            AiChatMessage message = latest.get(i);
            history.add(Map.of("role", message.getRole(), "content", message.getContent()));
        }
        return objectMapper.writeValueAsString(history);
    }

    private StudentProfile requireStudent(Long accountId) {
        StudentProfile student = studentProfileService.findByAccountId(accountId);
        if (student == null) {
            throw ApiException.forbidden("Student profile not found");
        }
        return student;
    }

    private AiChatConversation requireOwnConversation(Long accountId, Long conversationId) {
        StudentProfile student = requireStudent(accountId);
        AiChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !student.getId().equals(conversation.getStudentId())) {
            throw ApiException.notFound("AI conversation not found");
        }
        return conversation;
    }

    private void sendDelta(SseEmitter emitter, String content) {
        try {
            emitter.send(SseEmitter.event().name("delta").data(Map.of("content", content)));
        } catch (Exception ignored) {
        }
    }

    private void sendDone(SseEmitter emitter, AiStreamDoneResponse done) {
        try {
            emitter.send(SseEmitter.event().name("done").data(done));
        } catch (Exception ignored) {
        } finally {
            emitter.complete();
        }
    }

    private void sendError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(Map.of("message", message)));
        } catch (Exception ignored) {
        } finally {
            emitter.complete();
        }
    }

    private String titleFrom(String message) {
        String text = message.trim().replaceAll("\\s+", " ");
        return text.length() <= 24 ? text : text.substring(0, 24) + "...";
    }

    private AiConversationResponse mapConversation(AiChatConversation conversation) {
        return new AiConversationResponse(conversation.getId(), conversation.getTitle(), conversation.getCreatedAt(), conversation.getUpdatedAt());
    }

    private AiMessageResponse mapMessage(AiChatMessage message) {
        return new AiMessageResponse(message.getId(), message.getConversationId(), message.getRole(), message.getContent(), message.getSourcesJson(), message.getCreatedAt());
    }
}
