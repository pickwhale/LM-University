package com.university.backend.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminStudentRequest(
    @NotBlank(message = "Student ID cannot be empty.")
    @Size(max = 20, message = "Student ID must be less than 20 characters")
    String studentNo,
    @Size(min = 6, max = 20, message = "Password length must be between 6 and 20 characters.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
            message = "Passwords must contain letters and numbers"
    )String password,
    @NotBlank(message = "Student names cannot be empty.")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]+$", message = "Usernames can only contain letters, numbers, and underscores.")
    String fullName,
    String avatarPath,
    @Pattern(regexp = "^(Male|Female)$", message = "Gender must be Male or Female")
    String gender,
    @Size(max = 100, message = "College name must be less than 100 characters")
    String college,
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid contact number format")
    String contactNumber,
    Double score
) {

    public AdminStudentRequest {
        if (studentNo != null) studentNo = studentNo.strip();
        if (fullName != null) fullName = fullName.strip();
        if (contactNumber != null) contactNumber = contactNumber.strip();
        if (gender != null) {
            String g = gender.strip().toLowerCase();
            gender = switch (g) {
                case "male" -> "Male";
                case "female" -> "Female";
                default -> gender; // 让校验注解报错
            };
        }
    }
}
