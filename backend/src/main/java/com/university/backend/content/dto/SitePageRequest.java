package com.university.backend.content.dto;

import jakarta.validation.constraints.NotBlank;

public record SitePageRequest(
    @NotBlank(message = "Slug is required") String slug,
    @NotBlank(message = "Title is required") String title,
    String subtitle,
    @NotBlank(message = "Content is required") String content,
    String picture1Path,
    String picture2Path,
    String picture3Path
) {
}
