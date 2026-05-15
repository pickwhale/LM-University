package com.university.backend.auth.dto;

import com.university.backend.account.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @NotBlank(message = "Username is required") String username,
    @NotBlank(message = "Password is required") String password,
    @NotNull(message = "Login role is required") Role role
) {
}
