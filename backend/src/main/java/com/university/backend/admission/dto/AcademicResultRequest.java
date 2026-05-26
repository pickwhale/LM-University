package com.university.backend.admission.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AcademicResultRequest(
    @NotNull(message = "Student id is required") long studentId,
    @NotBlank(message = "Report number must not be blank")
    @Size(max = 50, message = "Report number must be less than 50 characters")
    String reportNo,
    @NotBlank(message = "Report content must not be blank") String reportContent,
    @Min(value = 0, message = "Grade must be more than 0")
    @Max(value = 1000, message = "Grade must be less than 1000")
    Integer grade,
    @Size(max = 100, message = "Evaluation must be less than 30 characters")
    String gradeEvaluation,
    @PastOrPresent(message = "Entered date must be in the past or today")
    LocalDate enteredAt

) {
}
