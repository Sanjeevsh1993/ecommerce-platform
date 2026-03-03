package com.ecommerce.shared.util;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent — old system had no correlation ID tracking
// BC References: BC-046 (X-Correlation-ID header propagation), BC-047 (MDC logging)

import com.ecommerce.shared.constants.AppConstants;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utilities for managing the X-Correlation-ID request tracking header.
 *
 * The correlation ID is:
 * 1. Generated at api-gateway if not present in incoming request (BC-046)
 * 2. Stored in MDC so all log statements automatically include it (BC-047)
 * 3. Returned in every API error response body as "correlationId" (BC-043)
 * 4. Propagated to downstream service calls via header
 */
public final class CorrelationIdUtils {

    private CorrelationIdUtils() {}

    /** Generate a new UUID-based correlation ID */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /** Store correlation ID in MDC for log enrichment (BC-047) */
    public static void setMdc(String correlationId) {
        MDC.put(AppConstants.MDC_CORRELATION_ID, correlationId);
    }

    /** Retrieve current correlation ID from MDC */
    public static String fromMdc() {
        String id = MDC.get(AppConstants.MDC_CORRELATION_ID);
        return id != null ? id : "unknown";
    }

    /** Clear MDC after request completes (prevent thread-local leaks in thread pools) */
    public static void clearMdc() {
        MDC.remove(AppConstants.MDC_CORRELATION_ID);
    }

    /**
     * Return the given value if non-blank, otherwise generate a new ID.
     * Used by gateway and service filters to accept or create a correlation ID.
     */
    public static String resolveOrGenerate(String incoming) {
        return (incoming != null && !incoming.isBlank()) ? incoming : generate();
    }
}
