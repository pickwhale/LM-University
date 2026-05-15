package com.university.backend.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UniversityRequest(
    @NotBlank(message = "University name is required") String name,
    String website,
    String imagePath,
    @NotNull(message = "Province is required") Long provinceId,
    String institutionType,
    String keyness,
    String introduction,
    String phone
) {
}
