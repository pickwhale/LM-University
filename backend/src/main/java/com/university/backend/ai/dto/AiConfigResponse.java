package com.university.backend.ai.dto;

public record AiConfigResponse(
    boolean enabled,
    String providerName,
    String endpointUrl,
    String httpMethod,
    boolean apiKeySet,
    String headersTemplate,
    String bodyTemplate,
    String model,
    double temperature,
    int maxTokens,
    String systemPrompt,
    String streamProtocol,
    String responseTextPath,
    String doneMarker,
    int timeoutSeconds
) {
}
