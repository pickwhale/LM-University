package com.university.backend.student.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminStudentRequest(
    @NotBlank(message = "学号不能为空") String studentNo,
    String password,
    @NotBlank(message = "学生姓名不能为空") String fullName,
    String avatarPath,
    String gender,
    String college,
    String contactNumber,
    Double score
) {
}
