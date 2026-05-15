package com.university.backend.content.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record NewsArticleRequest(
    @NotBlank(message = "Title is required") String title,
    String introduction,
    String picturePath,
    @NotBlank(message = "Content is required") String content,
    LocalDateTime publishedAt
) {
}
