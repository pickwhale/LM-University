package com.university.backend.legacy;

import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

public final class LegacyStatusMapper {

    public static final String LEGACY_PENDING = "\u5f85\u5ba1\u6838";
    public static final String LEGACY_APPROVED = "\u5df2\u901a\u8fc7";
    public static final String LEGACY_REJECTED = "\u5df2\u62d2\u7edd";

    public static final List<String> PENDING_VALUES = List.of(
        LEGACY_PENDING,
        "\u5f85\u5ba1",
        "\u672a\u5ba1\u6838"
    );

    public static final List<String> APPROVED_VALUES = List.of(
        LEGACY_APPROVED,
        "\u901a\u8fc7",
        "\u662f",
        "\u5ba1\u6838\u901a\u8fc7"
    );

    public static final List<String> REJECTED_VALUES = List.of(
        LEGACY_REJECTED,
        "\u62d2\u7edd",
        "\u5426",
        "\u9a73\u56de",
        "\u5ba1\u6838\u672a\u901a\u8fc7",
        "\u4e0d\u901a\u8fc7"
    );

    private LegacyStatusMapper() {
    }

    public static String toApiStatus(String legacyStatus) {
        if (!StringUtils.hasText(legacyStatus)) {
            return "PENDING";
        }
        String normalized = legacyStatus.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("APPROV") || normalized.contains("PASS") || containsAny(legacyStatus, APPROVED_VALUES)) {
            return "APPROVED";
        }
        if (normalized.contains("REJECT") || normalized.contains("DENY") || containsAny(legacyStatus, REJECTED_VALUES)) {
            return "REJECTED";
        }
        return "PENDING";
    }

    public static String toLegacyStatus(String apiStatus) {
        if (!StringUtils.hasText(apiStatus)) {
            return LEGACY_PENDING;
        }
        return switch (apiStatus.trim().toUpperCase(Locale.ROOT)) {
            case "APPROVED" -> LEGACY_APPROVED;
            case "REJECTED" -> LEGACY_REJECTED;
            default -> LEGACY_PENDING;
        };
    }

    private static boolean containsAny(String value, List<String> candidates) {
        return candidates.stream().anyMatch(value::contains);
    }
}
