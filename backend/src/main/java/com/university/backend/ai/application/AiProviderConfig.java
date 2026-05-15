package com.university.backend.ai.application;

public record AiProviderConfig(
    boolean enabled,
    String providerName,
    String endpointUrl,
    String httpMethod,
    String apiKey,
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
