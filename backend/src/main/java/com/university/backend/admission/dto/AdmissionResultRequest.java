package com.university.backend.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AdmissionResultRequest(
    @NotNull(message = "Application id is required") String applicationId,
    @NotBlank(message = "Result status is required") String resultStatus,
    @Size(max = 100, message = "Evaluation must be less than 30 characters")
    String feedback,
    @PastOrPresent(message = "Entered date must be in the past or today")
    LocalDateTime feedbackAt
) {
}
