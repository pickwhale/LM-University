package com.university.backend.admission.controller;

import com.university.backend.admission.application.AdmissionService;
import com.university.backend.admission.domain.AcademicResult;
import com.university.backend.admission.domain.AdmissionResult;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student")
public class StudentResultController {

    private final AdmissionService admissionService;

    public StudentResultController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping("/admission-results")
    public ApiResponse<PageResponse<AdmissionResult>> admissionResults(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(admissionService.pageOwnAdmission(account.accountId(), page, size)));
    }

    @GetMapping("/academic-results")
    public ApiResponse<PageResponse<AcademicResult>> academicResults(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(admissionService.pageOwnAcademic(account.accountId(), page, size)));
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
