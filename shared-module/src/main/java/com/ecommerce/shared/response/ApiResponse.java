package com.ecommerce.shared.response;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent — old system returned JSP views, not JSON
// BC References: BC-043 (standard error response format for all services)

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Generic API response wrapper used by ALL microservices.
 *
 * Success format:
 * {
 *   "success": true,
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "correlationId": "uuid",
 *   "data": { ... }
 * }
 *
 * Error format (BC-043):
 * {
 *   "success": false,
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "correlationId": "uuid",
 *   "error": {
 *     "code": "USER_NOT_FOUND",
 *     "message": "User with id 123 not found",
 *     "details": []
 *   }
 * }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String timestamp;
    private final String correlationId;
    private final T data;
    private final ErrorDetail error;

    @Builder
    private ApiResponse(boolean success, String timestamp, String correlationId,
                        T data, ErrorDetail error) {
        this.success = success;
        this.timestamp = timestamp;
        this.correlationId = correlationId;
        this.data = data;
        this.error = error;
    }

    // ── Factory: success with data ───────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(Instant.now().toString())
                .correlationId(correlationId)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    // ── Factory: error ───────────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String code, String message,
                                           List<String> details, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .timestamp(Instant.now().toString())
                .correlationId(correlationId)
                .error(ErrorDetail.of(code, message, details))
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String correlationId) {
        return error(code, message, List.of(), correlationId);
    }

    // ── Inner: error detail ──────────────────────────────────────────────────

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class ErrorDetail {

        private final String code;
        private final String message;
        private final List<String> details;

        private ErrorDetail(String code, String message, List<String> details) {
            this.code = code;
            this.message = message;
            this.details = details == null ? List.of() : List.copyOf(details);
        }

        public static ErrorDetail of(String code, String message, List<String> details) {
            return new ErrorDetail(code, message, details);
        }
    }
}
