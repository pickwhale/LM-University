package com.university.backend.content.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NewsArticleRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,
    @Size(max = 500, message = "Introduction must not exceed 500 characters")
    String introduction,
    String picturePath,
    @NotBlank(message = "Content is required")
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    String content,
    @FutureOrPresent(message = "Published time must be in the present or future")
    LocalDateTime publishedAt
) {
}
