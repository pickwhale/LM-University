package com.university.backend.auth.controller;

import com.university.backend.auth.application.AuthService;
import com.university.backend.auth.dto.AuthTokenResponse;
import com.university.backend.auth.dto.LoginRequest;
import com.university.backend.auth.dto.MeResponse;
import com.university.backend.auth.dto.RefreshTokenRequest;
import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.error.ApiException;
import com.university.backend.common.security.AuthenticatedAccount;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAccount account)) {
            throw ApiException.unauthorized("Unauthorized");
        }
        return ApiResponse.ok(authService.currentUser(account));
    }
}
