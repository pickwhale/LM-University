package com.university.backend.auth.dto;

import com.university.backend.account.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Usernames can only contain letters, numbers, and underscores.")
    String username,
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password length must be between 6 and 20 characters.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
            message = "Passwords must contain letters and numbers"
    )
    String password,
    @NotNull(message = "Login role is required") Role role

) {
}
