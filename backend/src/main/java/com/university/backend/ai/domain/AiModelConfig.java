package com.university.backend.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_model_config")
public class AiModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Boolean enabled;

    @TableField("provider_name")
    private String providerName;

    @TableField("endpoint_url")
    private String endpointUrl;

    @TableField("http_method")
    private String httpMethod;

    @TableField("api_key")
    private String apiKey;

    @TableField("headers_template")
    private String headersTemplate;

    @TableField("body_template")
    private String bodyTemplate;

    private String model;

    private Double temperature;

    @TableField("max_tokens")
    private Integer maxTokens;

    @TableField("system_prompt")
    private String systemPrompt;

    @TableField("stream_protocol")
    private String streamProtocol;

    @TableField("response_text_path")
    private String responseTextPath;

    @TableField("done_marker")
    private String doneMarker;

    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHeadersTemplate() {
        return headersTemplate;
    }

    public void setHeadersTemplate(String headersTemplate) {
        this.headersTemplate = headersTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getStreamProtocol() {
        return streamProtocol;
    }

    public void setStreamProtocol(String streamProtocol) {
        this.streamProtocol = streamProtocol;
    }

    public String getResponseTextPath() {
        return responseTextPath;
    }

    public void setResponseTextPath(String responseTextPath) {
        this.responseTextPath = responseTextPath;
    }

    public String getDoneMarker() {
        return doneMarker;
    }

    public void setDoneMarker(String doneMarker) {
        this.doneMarker = doneMarker;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
