package com.university.backend.recommendation.dto;

public record RecommendationItem(
    Long id,
    String type,
    String name,
    String universityName,
    String province,
    String institutionType,
    String majorCode,
    String imagePath,
    Double latestGrade,
    Integer cutOffScore,
    Double margin,
    String recommendationType,
    String reason,
    double score,
    Integer clickCount,
    Integer enrollmentQuota
) {
}
