package com.university.backend.system.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.system.application.AdminDashboardService;
import com.university.backend.system.dto.AdminDashboardSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ApiResponse<AdminDashboardSummary> summary() {
        return ApiResponse.ok(adminDashboardService.summary());
    }
}
