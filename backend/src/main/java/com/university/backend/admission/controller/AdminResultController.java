package com.university.backend.admission.controller;

import com.university.backend.admission.application.AdmissionService;
import com.university.backend.admission.domain.AcademicResult;
import com.university.backend.admission.domain.AdmissionResult;
import com.university.backend.admission.dto.AcademicResultRequest;
import com.university.backend.admission.dto.AdmissionResultRequest;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminResultController {

    private final AdmissionService admissionService;

    public AdminResultController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @GetMapping("/admission-results")
    public ApiResponse<PageResponse<AdmissionResult>> admissionResults(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(PageResponse.from(admissionService.pageAdminAdmission(page, size)));
    }

    @PostMapping("/admission-results")
    public ApiResponse<AdmissionResult> createAdmissionResult(@Valid @RequestBody AdmissionResultRequest request) {
        return ApiResponse.ok(admissionService.createAdmission(request));
    }

    @PutMapping("/admission-results/{id}")
    public ApiResponse<AdmissionResult> updateAdmissionResult(
        @PathVariable Long id,
        @Valid @RequestBody AdmissionResultRequest request
    ) {
        return ApiResponse.ok(admissionService.updateAdmission(id, request));
    }

    @GetMapping("/academic-results")
    public ApiResponse<PageResponse<AcademicResult>> academicResults(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(PageResponse.from(admissionService.pageAdminAcademic(page, size)));
    }

    @PostMapping("/academic-results")
    public ApiResponse<AcademicResult> createAcademicResult(@Valid @RequestBody AcademicResultRequest request) {
        return ApiResponse.ok(admissionService.createAcademic(request));
    }

    @PutMapping("/academic-results/{id}")
    public ApiResponse<AcademicResult> updateAcademicResult(
        @PathVariable Long id,
        @Valid @RequestBody AcademicResultRequest request
    ) {
        return ApiResponse.ok(admissionService.updateAcademic(id, request));
    }
}
