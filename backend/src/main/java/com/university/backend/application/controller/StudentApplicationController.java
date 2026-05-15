package com.university.backend.application.controller;

import com.university.backend.application.application.MajorApplicationService;
import com.university.backend.application.application.UniversityApplicationService;
import com.university.backend.application.domain.MajorApplication;
import com.university.backend.application.domain.UniversityApplication;
import com.university.backend.application.dto.MajorApplicationRequest;
import com.university.backend.application.dto.UniversityApplicationRequest;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student")
public class StudentApplicationController {

    private final UniversityApplicationService universityApplicationService;
    private final MajorApplicationService majorApplicationService;

    public StudentApplicationController(
        UniversityApplicationService universityApplicationService,
        MajorApplicationService majorApplicationService
    ) {
        this.universityApplicationService = universityApplicationService;
        this.majorApplicationService = majorApplicationService;
    }

    @GetMapping("/university-applications")
    public ApiResponse<PageResponse<UniversityApplication>> universityApplications(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(universityApplicationService.pageOwn(account.accountId(), page, size)));
    }

    @PostMapping("/university-applications")
    public ApiResponse<UniversityApplication> createUniversityApplication(
        Authentication authentication,
        @Valid @RequestBody UniversityApplicationRequest request
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(universityApplicationService.create(account.accountId(), request));
    }

    @GetMapping("/major-applications")
    public ApiResponse<PageResponse<MajorApplication>> majorApplications(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(majorApplicationService.pageOwn(account.accountId(), page, size)));
    }

    @PostMapping("/major-applications")
    public ApiResponse<MajorApplication> createMajorApplication(
        Authentication authentication,
        @Valid @RequestBody MajorApplicationRequest request
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(majorApplicationService.create(account.accountId(), request));
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
