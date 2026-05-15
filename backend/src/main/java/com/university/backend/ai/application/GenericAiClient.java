package com.university.backend.ai.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.backend.common.error.ApiException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GenericAiClient {

    private final ObjectMapper objectMapper;

    public GenericAiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void stream(
        AiProviderConfig config,
        String message,
        String context,
        String historyJson,
        Consumer<String> onDelta
    ) throws Exception {
        HttpRequest request = buildRequest(config, message, context, historyJson);
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.max(3, config.timeoutSeconds())))
            .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("AI API request failed: HTTP " + response.statusCode() + " " + body);
        }
        parseStream(config, response.body(), onDelta);
    }

    private HttpRequest buildRequest(
        AiProviderConfig config,
        String message,
        String context,
        String historyJson
    ) throws Exception {
        if (!StringUtils.hasText(config.endpointUrl())) {
            throw ApiException.badRequest("AI endpoint is not configured");
        }
        if (!"POST".equalsIgnoreCase(config.httpMethod())) {
            throw ApiException.badRequest("Only POST AI API calls are supported");
        }
        String body = render(config.bodyTemplate(), config, message, context, historyJson);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(config.endpointUrl()))
            .timeout(Duration.ofSeconds(Math.max(3, config.timeoutSeconds())))
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

        Map<String, Object> headers = objectMapper.readValue(
            render(config.headersTemplate(), config, message, context, historyJson),
            new TypeReference<>() {
            }
        );
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (StringUtils.hasText(entry.getKey()) && entry.getValue() != null) {
                builder.header(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return builder.build();
    }

    private String render(String template, AiProviderConfig config, String message, String context, String historyJson) {
        String rendered = template == null ? "" : template;
        rendered = rendered.replace("{{apiKey}}", escape(config.apiKey()));
        rendered = rendered.replace("{{message}}", escape(message));
        rendered = rendered.replace("{{systemPrompt}}", escape(withFormatInstruction(config.systemPrompt())));
        rendered = rendered.replace("{{context}}", escape(context));
        rendered = rendered.replace("{{model}}", escape(config.model()));
        rendered = rendered.replace("{{historyJson}}", historyJson == null ? "[]" : historyJson);
        rendered = rendered.replace("{{temperature}}", String.valueOf(config.temperature()));
        rendered = rendered.replace("{{maxTokens}}", String.valueOf(config.maxTokens()));
        return rendered;
    }

    private String escape(String value) {
        try {
            String json = objectMapper.writeValueAsString(value == null ? "" : value);
            return json.substring(1, json.length() - 1);
        } catch (Exception ignored) {
            return value == null ? "" : value.replace("\"", "\\\"");
        }
    }

    private String withFormatInstruction(String systemPrompt) {
        String basePrompt = systemPrompt == null ? "" : systemPrompt.trim();
        String formatPrompt = """

            【输出格式要求】
            1. 必须使用结构化 Markdown 格式，不要输出原始 HTML 标签。
            2. 院校和专业必须分开列出，使用清晰的标题分隔。
            3. 使用项目符号列表（- 或 *），不要使用编号列表（1. 2. 3.）。
            4. 【强制要求】每两个院校之间必须插入两个换行符（空一行），每两个专业之间也必须插入两个换行符。
            5. 例如：
               - **学校A**
                 - **字段1**：值1
                 - **字段2**：值2
               
               - **学校B**
                 - **字段1**：值1
                 - **字段2**：값2
            6. 字段名使用加粗，如 **录取分数线**、**分数优势**。
            7. 推荐院校和推荐专业必须分别用二级标题标注（## 推荐院校、## 推荐专业）。
            8. 回答结尾给出一句简短的填报建议。
            """;
        return basePrompt + formatPrompt;
    }

    private void parseStream(AiProviderConfig config, InputStream inputStream, Consumer<String> onDelta) throws Exception {
        String protocol = StringUtils.hasText(config.streamProtocol()) ? config.streamProtocol().toUpperCase() : "AUTO";
        if ("TEXT".equals(protocol)) {
            streamText(inputStream, onDelta);
            return;
        }
        streamSseLike(config, inputStream, onDelta, "AUTO".equals(protocol));
    }

    private void streamText(InputStream inputStream, Consumer<String> onDelta) throws Exception {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            if (read > 0) {
                onDelta.accept(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
        }
    }

    private void streamSseLike(AiProviderConfig config, InputStream inputStream, Consumer<String> onDelta, boolean auto) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!StringUtils.hasText(trimmed)) {
                    continue;
                }
                if (trimmed.startsWith("data:")) {
                    String data = trimmed.substring(5).trim();
                    if (data.equals(config.doneMarker())) {
                        break;
                    }
                    String delta = extractDelta(data, config.responseTextPath());
                    if (StringUtils.hasText(delta)) {
                        onDelta.accept(delta);
                    } else if (auto) {
                        onDelta.accept(data);
                    }
                } else if (auto) {
                    onDelta.accept(trimmed);
                }
            }
        }
    }

    private String extractDelta(String data, String responseTextPath) throws Exception {
        if (!StringUtils.hasText(responseTextPath)) {
            return data;
        }
        JsonNode node = objectMapper.readTree(data);
        JsonNode current = node;
        for (String segment : responseTextPath.split("\\.")) {
            if (!StringUtils.hasText(segment) || current == null) {
                return "";
            }
            if (segment.chars().allMatch(Character::isDigit)) {
                current = current.path(Integer.parseInt(segment));
            } else {
                current = current.path(segment);
            }
        }
        return current == null || current.isMissingNode() || current.isNull() ? "" : current.asText("");
    }
}
