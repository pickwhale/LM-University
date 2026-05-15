package com.university.backend.system.dto;

public record AdminDashboardSummary(
    long provinceCount,
    long studentCount,
    long universityCount,
    long majorCount,
    long universityApplicationCount,
    long pendingUniversityApplicationCount,
    long majorApplicationCount,
    long pendingMajorApplicationCount,
    long newsCount,
    long pageCount,
    long settingCount
) {
}
