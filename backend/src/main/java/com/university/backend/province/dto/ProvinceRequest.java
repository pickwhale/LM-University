package com.university.backend.province.dto;

import jakarta.validation.constraints.NotBlank;

public record ProvinceRequest(@NotBlank(message = "Province name is required") String name) {
}
