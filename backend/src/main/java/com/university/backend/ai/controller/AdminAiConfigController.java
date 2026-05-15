package com.university.backend.ai.controller;

import com.university.backend.ai.application.AiConfigService;
import com.university.backend.ai.application.AiProviderConfig;
import com.university.backend.ai.application.GenericAiClient;
import com.university.backend.ai.dto.AiConfigRequest;
import com.university.backend.ai.dto.AiConfigResponse;
import com.university.backend.ai.dto.AiConfigTestRequest;
import com.university.backend.ai.dto.AiConfigTestResponse;
import com.university.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/ai-config")
public class AdminAiConfigController {

    private final AiConfigService aiConfigService;
    private final GenericAiClient genericAiClient;

    public AdminAiConfigController(AiConfigService aiConfigService, GenericAiClient genericAiClient) {
        this.aiConfigService = aiConfigService;
        this.genericAiClient = genericAiClient;
    }

    @GetMapping
    public ApiResponse<AiConfigResponse> config() {
        return ApiResponse.ok(aiConfigService.getResponse());
    }

    @PutMapping
    public ApiResponse<AiConfigResponse> update(@Valid @RequestBody AiConfigRequest request) {
        return ApiResponse.ok(aiConfigService.update(request));
    }

    @PostMapping("/test")
    public ApiResponse<AiConfigTestResponse> test(@RequestBody(required = false) AiConfigTestRequest request) {
        try {
            AiProviderConfig config = aiConfigService.getConfig();
            StringBuilder answer = new StringBuilder();
            String message = request == null || request.message() == null ? "请回复：连接测试成功" : request.message();
            genericAiClient.stream(config, message, "后台 AI 配置连接测试，不包含学生隐私数据。", "[]", delta -> {
                if (answer.length() < 500) {
                    answer.append(delta);
                }
            });
            return ApiResponse.ok(new AiConfigTestResponse(true, answer.toString()));
        } catch (Exception exception) {
            return ApiResponse.ok(new AiConfigTestResponse(false, exception.getMessage() == null ? "AI connection test failed" : exception.getMessage()));
        }
    }
}
