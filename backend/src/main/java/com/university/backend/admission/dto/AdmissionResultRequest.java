package com.university.backend.admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AdmissionResultRequest(
    @NotNull(message = "Application id is required") Long applicationId,
    @NotBlank(message = "Result status is required") String resultStatus,
    String feedback,
    LocalDateTime feedbackAt
) {
}
