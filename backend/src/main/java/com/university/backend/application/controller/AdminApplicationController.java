package com.university.backend.application.controller;

import com.university.backend.application.application.MajorApplicationService;
import com.university.backend.application.application.UniversityApplicationService;
import com.university.backend.application.domain.MajorApplication;
import com.university.backend.application.domain.UniversityApplication;
import com.university.backend.application.dto.ApplicationReviewRequest;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminApplicationController {

    private final UniversityApplicationService universityApplicationService;
    private final MajorApplicationService majorApplicationService;

    public AdminApplicationController(
        UniversityApplicationService universityApplicationService,
        MajorApplicationService majorApplicationService
    ) {
        this.universityApplicationService = universityApplicationService;
        this.majorApplicationService = majorApplicationService;
    }

    @GetMapping("/university-applications")
    public ApiResponse<PageResponse<UniversityApplication>> universityApplications(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(PageResponse.from(universityApplicationService.pageAdmin(page, size, status)));
    }

    @PutMapping("/university-applications/{id}/review")
    public ApiResponse<UniversityApplication> reviewUniversityApplication(
        @PathVariable Long id,
        @Valid @RequestBody ApplicationReviewRequest request
    ) {
        return ApiResponse.ok(universityApplicationService.review(id, request));
    }

    @GetMapping("/major-applications")
    public ApiResponse<PageResponse<MajorApplication>> majorApplications(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(PageResponse.from(majorApplicationService.pageAdmin(page, size, status)));
    }

    @PutMapping("/major-applications/{id}/review")
    public ApiResponse<MajorApplication> reviewMajorApplication(
        @PathVariable Long id,
        @Valid @RequestBody ApplicationReviewRequest request
    ) {
        return ApiResponse.ok(majorApplicationService.review(id, request));
    }
}
