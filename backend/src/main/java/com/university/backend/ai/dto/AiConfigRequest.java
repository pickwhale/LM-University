package com.university.backend.ai.dto;

public record AiConfigRequest(
    Boolean enabled,
    String providerName,
    String endpointUrl,
    String httpMethod,
    String apiKey,
    String headersTemplate,
    String bodyTemplate,
    String model,
    Double temperature,
    Integer maxTokens,
    String systemPrompt,
    String streamProtocol,
    String responseTextPath,
    String doneMarker,
    Integer timeoutSeconds
) {
}
