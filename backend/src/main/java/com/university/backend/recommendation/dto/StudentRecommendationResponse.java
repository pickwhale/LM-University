package com.university.backend.recommendation.dto;

import java.util.List;

public record StudentRecommendationResponse(
    Double latestGrade,
    String message,
    List<RecommendationItem> universities,
    List<RecommendationItem> majors
) {
}
