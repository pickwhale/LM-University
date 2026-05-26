package com.university.backend.major.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.major.application.MajorService;
import com.university.backend.major.domain.Major;
import com.university.backend.major.dto.MajorRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("/api/v1/admin/majors")
public class AdminMajorController {

    private final MajorService majorService;

    public AdminMajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Major>> majors(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false)
        @Pattern(regexp = "^[a-zA-Z -]*$", message = "搜索关键词只能包含字母、空格和连字符")
        String keyword
    ) {
        return ApiResponse.ok(PageResponse.from(majorService.pageAdmin(page, size, keyword)));
    }

    @PostMapping
    public ApiResponse<Major> create(@Valid @RequestBody MajorRequest request) {
        return ApiResponse.ok(majorService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Major> update(@PathVariable Long id, @Valid @RequestBody MajorRequest request) {
        return ApiResponse.ok(majorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        majorService.delete(id);
        return ApiResponse.ok();
    }
}
