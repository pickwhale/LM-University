package com.university.backend.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UniversityRequest(
    @NotBlank(message = "University name is required")
    @Size(max = 200, message = "University name must be less than 200 characters")
    String name,
    @Pattern(regexp = "^(https?://).+", message = "Website must start with http:// or https://")
    @Size(max = 500, message = "Website URL must be less than 500 characters")
    String website,
    String imagePath,
    @NotNull(message = "Province is required") Long provinceId,
    @Pattern(regexp = "^(?i)(public|private)$", message = "Institution type must be 'public' or 'private'")
    String institutionType,
    @Size(max = 50, message = "Keyness must be less than 50 characters")
    String keyness,
    @Size(max = 2000, message = "Introduction must be less than 2000 characters")
    String introduction,
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid contact number format")
    String phone
) { public UniversityRequest {
    // 去除字符串首尾空白
    if (name != null) name = name.strip();
    if (website != null) website = website.strip();
    if (imagePath != null) imagePath = imagePath.strip();
    if (keyness != null) keyness = keyness.strip();
    if (introduction != null) introduction = introduction.strip();
    if (phone != null) phone = phone.strip();

    // 规范化 institutionType，类似 AdminStudentRequest 中 gender 的处理
    if (institutionType != null) {
        String cleaned = institutionType.strip().toLowerCase();
        institutionType = switch (cleaned) {
            case "public" -> "Public";
            case "private" -> "Private";
            default -> institutionType; // 不合法的值保留原样，让 @Pattern 校验失败并返回错误信息
        };
    }
}
}
