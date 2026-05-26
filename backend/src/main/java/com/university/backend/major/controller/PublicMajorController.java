package com.university.backend.major.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.major.application.MajorService;
import com.university.backend.major.domain.Major;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/majors")
public class PublicMajorController {

    private final MajorService majorService;

    public PublicMajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Major>> majors(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false)
        @Pattern(regexp = "^[a-zA-Z -]*$", message = "搜索关键词只能包含字母、空格和连字符")
        String keyword,
        @RequestParam(required = false) Long universityId
    ) {
        return ApiResponse.ok(PageResponse.from(majorService.pagePublic(page, size, keyword, universityId)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Major> major(@PathVariable Long id) {
        return ApiResponse.ok(majorService.getRequired(id));
    }
}
