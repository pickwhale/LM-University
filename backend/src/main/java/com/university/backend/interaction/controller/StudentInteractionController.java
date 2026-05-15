package com.university.backend.interaction.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.api.PageResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import com.university.backend.interaction.application.ConsultationService;
import com.university.backend.interaction.application.FavoriteService;
import com.university.backend.interaction.domain.Consultation;
import com.university.backend.interaction.domain.Favorite;
import com.university.backend.interaction.dto.ConsultationRequest;
import com.university.backend.interaction.dto.FavoriteRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student")
public class StudentInteractionController {

    private final ConsultationService consultationService;
    private final FavoriteService favoriteService;

    public StudentInteractionController(ConsultationService consultationService, FavoriteService favoriteService) {
        this.consultationService = consultationService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/consultations")
    public ApiResponse<PageResponse<Consultation>> consultations(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(consultationService.pageOwn(account.accountId(), page, size)));
    }

    @PostMapping("/consultations")
    public ApiResponse<Consultation> createConsultation(
        Authentication authentication,
        @Valid @RequestBody ConsultationRequest request
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(consultationService.create(account.accountId(), request));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<Favorite>> favorites(
        Authentication authentication,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "10") long size
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(PageResponse.from(favoriteService.pageOwn(account.accountId(), page, size)));
    }

    @PostMapping("/favorites")
    public ApiResponse<Favorite> createFavorite(
        Authentication authentication,
        @Valid @RequestBody FavoriteRequest request
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(favoriteService.create(account.accountId(), request));
    }

    @GetMapping("/favorites/check")
    public ApiResponse<Boolean> checkFavorite(
        Authentication authentication,
        @RequestParam String targetType,
        @RequestParam Long targetId
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(favoriteService.isFavorited(account.accountId(), targetType, targetId));
    }

    @DeleteMapping("/favorites/{id}")
    public ApiResponse<Void> deleteFavorite(Authentication authentication, @PathVariable Long id) {
        AuthenticatedAccount account = requireAccount(authentication);
        favoriteService.delete(account.accountId(), id);
        return ApiResponse.ok();
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
