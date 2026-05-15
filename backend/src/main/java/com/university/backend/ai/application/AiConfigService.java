package com.university.backend.ai.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.backend.ai.domain.AiModelConfig;
import com.university.backend.ai.dto.AiConfigRequest;
import com.university.backend.ai.dto.AiConfigResponse;
import com.university.backend.ai.infrastructure.AiModelConfigMapper;
import com.university.backend.common.error.ApiException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiConfigService {

    private static final String DEFAULT_HEADERS = """
        {
          "Content-Type": "application/json",
          "Authorization": "Bearer {{apiKey}}"
        }
        """;
    private static final String DEFAULT_BODY = """
        {
          "model": "{{model}}",
          "stream": true,
          "messages": [
            {
              "role": "system",
              "content": "{{systemPrompt}}\\n\\n数据库上下文：\\n{{context}}"
            },
            {
              "role": "user",
              "content": "{{message}}"
            }
          ],
          "temperature": {{temperature}},
          "max_tokens": {{maxTokens}}
        }
        """;
    private static final String DEFAULT_SYSTEM_PROMPT = """
        你是大学招生平台的 AI 助手，必须基于提供的数据库上下文回答。
        你可以读取公开院校、专业、资讯和当前登录学生自己的资料、报名、收藏、成绩、录取信息。
        不能编造数据库中不存在的学校、专业或学生隐私信息。没有数据时，请明确说明当前系统没有查询到相关信息。
        回答时使用结构化 Markdown：短段落、编号列表、项目符号列表、加粗字段名。不要把多个学校或专业挤在同一行。
        """;

    private final AiModelConfigMapper configMapper;

    public AiConfigService(AiModelConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    public AiProviderConfig getConfig() {
        try {
            AiModelConfig entity = findCurrent();
            if (entity == null) {
                return defaultConfig();
            }
            return new AiProviderConfig(
                Boolean.TRUE.equals(entity.getEnabled()),
                defaultText(entity.getProviderName(), "Custom API"),
                defaultText(entity.getEndpointUrl(), ""),
                defaultText(entity.getHttpMethod(), "POST"),
                defaultText(entity.getApiKey(), ""),
                defaultText(entity.getHeadersTemplate(), DEFAULT_HEADERS),
                defaultText(entity.getBodyTemplate(), DEFAULT_BODY),
                defaultText(entity.getModel(), ""),
                entity.getTemperature() == null ? 0.7 : entity.getTemperature(),
                entity.getMaxTokens() == null ? 1024 : entity.getMaxTokens(),
                defaultText(entity.getSystemPrompt(), DEFAULT_SYSTEM_PROMPT),
                defaultText(entity.getStreamProtocol(), "AUTO"),
                defaultText(entity.getResponseTextPath(), "choices.0.delta.content"),
                defaultText(entity.getDoneMarker(), "[DONE]"),
                entity.getTimeoutSeconds() == null ? 60 : entity.getTimeoutSeconds()
            );
        } catch (DataAccessException exception) {
            throw ApiException.badRequest("AI 配置表不存在，请先执行 docs/ai-chat-tables.sql 创建 ai_model_config 表。");
        }
    }

    public AiConfigResponse getResponse() {
        return toResponse(getConfig());
    }

    public AiConfigResponse update(AiConfigRequest request) {
        try {
            AiModelConfig existing = findCurrent();
            AiProviderConfig current = existing == null ? defaultConfig() : getConfig();
            AiModelConfig entity = existing == null ? new AiModelConfig() : existing;
            entity.setEnabled(request.enabled() == null ? current.enabled() : request.enabled());
            entity.setProviderName(defaultText(request.providerName(), current.providerName()));
            entity.setEndpointUrl(defaultText(request.endpointUrl(), current.endpointUrl()));
            entity.setHttpMethod("POST");
            entity.setApiKey(StringUtils.hasText(request.apiKey()) ? request.apiKey().trim() : current.apiKey());
            entity.setHeadersTemplate(defaultText(request.headersTemplate(), current.headersTemplate()));
            entity.setBodyTemplate(defaultText(request.bodyTemplate(), current.bodyTemplate()));
            entity.setModel(defaultText(request.model(), current.model()));
            entity.setTemperature(request.temperature() == null ? current.temperature() : request.temperature());
            entity.setMaxTokens(request.maxTokens() == null ? current.maxTokens() : request.maxTokens());
            entity.setSystemPrompt(defaultText(request.systemPrompt(), current.systemPrompt()));
            entity.setStreamProtocol(defaultText(request.streamProtocol(), current.streamProtocol()).toUpperCase());
            entity.setResponseTextPath(defaultText(request.responseTextPath(), current.responseTextPath()));
            entity.setDoneMarker(defaultText(request.doneMarker(), current.doneMarker()));
            entity.setTimeoutSeconds(request.timeoutSeconds() == null ? current.timeoutSeconds() : request.timeoutSeconds());
            if (entity.getId() == null) {
                configMapper.insert(entity);
            } else {
                configMapper.updateById(entity);
            }
            return getResponse();
        } catch (DataAccessException exception) {
            throw ApiException.badRequest("保存 AI 配置失败：请先执行 docs/ai-chat-tables.sql 创建或更新 ai_model_config 表。");
        }
    }

    private AiProviderConfig defaultConfig() {
        return new AiProviderConfig(
            false,
            "Custom API",
            "",
            "POST",
            "",
            DEFAULT_HEADERS,
            DEFAULT_BODY,
            "",
            0.7,
            1024,
            DEFAULT_SYSTEM_PROMPT,
            "AUTO",
            "choices.0.delta.content",
            "[DONE]",
            60
        );
    }

    private AiConfigResponse toResponse(AiProviderConfig config) {
        return new AiConfigResponse(
            config.enabled(),
            config.providerName(),
            config.endpointUrl(),
            "POST",
            StringUtils.hasText(config.apiKey()),
            config.headersTemplate(),
            config.bodyTemplate(),
            config.model(),
            config.temperature(),
            config.maxTokens(),
            config.systemPrompt(),
            config.streamProtocol(),
            config.responseTextPath(),
            config.doneMarker(),
            config.timeoutSeconds()
        );
    }

    private AiModelConfig findCurrent() {
        return configMapper.selectOne(new LambdaQueryWrapper<AiModelConfig>()
            .orderByAsc(AiModelConfig::getId)
            .last("limit 1"));
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
