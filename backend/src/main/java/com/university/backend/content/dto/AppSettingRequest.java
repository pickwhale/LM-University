package com.university.backend.content.dto;

import jakarta.validation.constraints.NotBlank;

public record AppSettingRequest(
    @NotBlank(message = "Setting key is required") String settingKey,
    String settingValue
) {
}
