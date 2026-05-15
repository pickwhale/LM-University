package com.university.backend.university.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.province.application.ProvinceService;
import com.university.backend.province.domain.Province;
import com.university.backend.province.dto.ProvinceRequest;
import com.university.backend.university.application.UniversityService;
import com.university.backend.university.domain.University;
import com.university.backend.university.dto.UniversityRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class AdminUniversityController {

    private final ProvinceService provinceService;
    private final UniversityService universityService;

    public AdminUniversityController(ProvinceService provinceService, UniversityService universityService) {
        this.provinceService = provinceService;
        this.universityService = universityService;
    }

    @GetMapping("/provinces")
    public ApiResponse<List<Province>> provinces() {
        return ApiResponse.ok(provinceService.listAll());
    }

    @PostMapping("/provinces")
    public ApiResponse<Province> createProvince(@Valid @RequestBody ProvinceRequest request) {
        return ApiResponse.ok(provinceService.create(request));
    }

    @PutMapping("/provinces/{id}")
    public ApiResponse<Province> updateProvince(@PathVariable Long id, @Valid @RequestBody ProvinceRequest request) {
        return ApiResponse.ok(provinceService.update(id, request));
    }

    @DeleteMapping("/provinces/{id}")
    public ApiResponse<Void> deleteProvince(@PathVariable Long id) {
        provinceService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/universities")
    public ApiResponse<PageResponse<University>> universities(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(PageResponse.from(universityService.pageAdmin(page, size, keyword)));
    }

    @PostMapping("/universities")
    public ApiResponse<University> createUniversity(@Valid @RequestBody UniversityRequest request) {
        return ApiResponse.ok(universityService.create(request));
    }

    @PutMapping("/universities/{id}")
    public ApiResponse<University> updateUniversity(@PathVariable Long id, @Valid @RequestBody UniversityRequest request) {
        return ApiResponse.ok(universityService.update(id, request));
    }

    @DeleteMapping("/universities/{id}")
    public ApiResponse<Void> deleteUniversity(@PathVariable Long id) {
        universityService.delete(id);
        return ApiResponse.ok();
    }
}
