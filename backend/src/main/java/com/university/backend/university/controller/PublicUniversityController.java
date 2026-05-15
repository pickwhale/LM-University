package com.university.backend.university.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.province.application.ProvinceService;
import com.university.backend.province.domain.Province;
import com.university.backend.university.application.UniversityService;
import com.university.backend.university.domain.University;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicUniversityController {

    private final ProvinceService provinceService;
    private final UniversityService universityService;

    public PublicUniversityController(ProvinceService provinceService, UniversityService universityService) {
        this.provinceService = provinceService;
        this.universityService = universityService;
    }

    @GetMapping("/provinces")
    public ApiResponse<List<Province>> provinces() {
        return ApiResponse.ok(provinceService.listAll());
    }

    @GetMapping("/universities")
    public ApiResponse<PageResponse<University>> universities(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long provinceId
    ) {
        return ApiResponse.ok(PageResponse.from(universityService.pagePublic(page, size, keyword, provinceId)));
    }

    @GetMapping("/universities/{id}")
    public ApiResponse<University> university(@PathVariable Long id) {
        return ApiResponse.ok(universityService.getRequired(id));
    }
}
