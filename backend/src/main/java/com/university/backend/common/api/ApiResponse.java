package com.university.backend.common.api;

import java.time.Instant;

public record ApiResponse<T>(boolean success, T data, Instant timestamp) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, Instant.now());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, Instant.now());
    }
}
