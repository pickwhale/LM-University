package com.university.backend.student.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import com.university.backend.student.application.StudentProfileService;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.student.dto.StudentProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student/profile")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    public StudentProfileController(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;
    }

    @GetMapping
    public ApiResponse<StudentProfile> profile(Authentication authentication) {
        AuthenticatedAccount account = requireAccount(authentication);
        StudentProfile profile = studentProfileService.findByAccountId(account.accountId());
        if (profile == null) {
            throw ApiException.notFound("Student profile not found");
        }
        return ApiResponse.ok(profile);
    }

    @PutMapping
    public ApiResponse<StudentProfile> update(
        Authentication authentication,
        @Valid @RequestBody StudentProfileUpdateRequest request
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        StudentProfile profile = studentProfileService.updateByAccountId(account.accountId(), request);
        if (profile == null) {
            throw ApiException.notFound("Student profile not found");
        }
        return ApiResponse.ok(profile);
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
