package com.university.backend.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.student.application.AdminStudentService;
import com.university.backend.student.domain.StudentProfile;
import com.university.backend.student.dto.AdminStudentRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/admin/students")
public class AdminStudentController {

    private final AdminStudentService studentService;

    public AdminStudentController(AdminStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ApiResponse<PageResponse<StudentProfile>> students(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size,
        @RequestParam(required = false) String keyword
    ) {
        Page<StudentProfile> result = studentService.page(page, size, keyword);
        return ApiResponse.ok(PageResponse.from(result));
    }

    @PostMapping
    public ApiResponse<StudentProfile> create(@Valid @RequestBody AdminStudentRequest request) {
        return ApiResponse.ok(studentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentProfile> update(@PathVariable Long id, @Valid @RequestBody AdminStudentRequest request) {
        return ApiResponse.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.ok();
    }
}
