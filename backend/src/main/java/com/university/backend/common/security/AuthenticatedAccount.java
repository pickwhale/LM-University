package com.university.backend.common.security;

import com.university.backend.account.domain.Role;

public record AuthenticatedAccount(Long accountId, String username, Role role) {
}
