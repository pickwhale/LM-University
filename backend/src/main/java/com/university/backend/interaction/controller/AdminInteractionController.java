package com.university.backend.interaction.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.interaction.application.ConsultationService;
import com.university.backend.interaction.domain.Consultation;
import com.university.backend.interaction.dto.ConsultationReplyRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/consultations")
public class AdminInteractionController {

    private final ConsultationService consultationService;

    public AdminInteractionController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Consultation>> consultations(
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(PageResponse.from(consultationService.pageAdmin(page, size)));
    }

    @PutMapping("/{id}/reply")
    public ApiResponse<Consultation> reply(
        @PathVariable Long id,
        @Valid @RequestBody ConsultationReplyRequest request
    ) {
        return ApiResponse.ok(consultationService.reply(id, request));
    }
}
