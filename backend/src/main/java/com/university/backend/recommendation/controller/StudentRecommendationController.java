package com.university.backend.recommendation.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import com.university.backend.recommendation.application.StudentRecommendationService;
import com.university.backend.recommendation.dto.StudentRecommendationResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/student")
public class StudentRecommendationController {

    private final StudentRecommendationService recommendationService;

    public StudentRecommendationController(StudentRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendations")
    public ApiResponse<StudentRecommendationResponse> recommendations(
        Authentication authentication,
        @RequestParam(defaultValue = "10") int limit
    ) {
        AuthenticatedAccount account = requireAccount(authentication);
        return ApiResponse.ok(recommendationService.recommend(account.accountId(), limit));
    }

    private AuthenticatedAccount requireAccount(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return account;
    }
}
