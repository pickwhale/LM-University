package com.university.backend.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoriteRequest(
    @NotBlank(message = "Target type is required") String targetType,
    @NotNull(message = "Target id is required") Long targetId,
    @NotBlank(message = "Name is required") String name,
    String picturePath,
    String recommendationType,
    String remark
) {
}
