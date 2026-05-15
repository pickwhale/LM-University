package com.university.backend.admission.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AcademicResultRequest(
    @NotNull(message = "Student id is required") Long studentId,
    String reportNo,
    String reportContent,
    Integer grade,
    String gradeEvaluation,
    LocalDate enteredAt
) {
}
