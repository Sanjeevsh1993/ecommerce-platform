package com.ecommerce.shared.util;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent — old system used Apache Commons Lang StringUtils
// BC References: BC-047 (log statement helpers)

/**
 * String utility methods.
 *
 * Thin wrapper providing null-safe string operations used across services.
 * The old project used Apache Commons Lang 3 for similar utilities.
 */
public final class StringUtils {

    private StringUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String trimToEmpty(String s) {
        return s == null ? "" : s.strip();
    }

    /** Truncate string to maxLength, appending "..." if truncated */
    public static String truncate(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 3) + "...";
    }

    /** Mask all but last 4 chars — used for logging sensitive IDs */
    public static String mask(String s) {
        if (isBlank(s) || s.length() <= 4) return "****";
        return "*".repeat(s.length() - 4) + s.substring(s.length() - 4);
    }
}
