package com.ecommerce.shared.response;

// BC References: BC-043 (standard response format)

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    // ── success() ────────────────────────────────────────────────────────────

    @Test
    void success_withData_returnsSuccessTrueAndDataSet() {
        ApiResponse<String> response = ApiResponse.success("hello", "corr-123");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getCorrelationId()).isEqualTo("corr-123");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void success_withoutCorrelationId_returnsNullCorrelationId() {
        ApiResponse<Integer> response = ApiResponse.success(42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(42);
        assertThat(response.getCorrelationId()).isNull();
    }

    // ── error() ───────────────────────────────────────────────────────────────

    @Test
    void error_withCodeAndMessage_returnsSuccessFalseAndErrorSet() {
        ApiResponse<Void> response = ApiResponse.error("USER_NOT_FOUND",
                "User not found", "corr-456");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getCorrelationId()).isEqualTo("corr-456");
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getError().getMessage()).isEqualTo("User not found");
        assertThat(response.getError().getDetails()).isEmpty();
    }

    @Test
    void error_withDetails_returnsDetailsInErrorBlock() {
        List<String> details = List.of("Field 'email' is required", "Field 'name' is required");
        ApiResponse<Void> response = ApiResponse.error("VALIDATION_ERROR",
                "Validation failed", details, "corr-789");

        assertThat(response.getError().getDetails()).containsExactlyElementsOf(details);
    }

    @Test
    void error_timestampIsAlwaysPopulated() {
        ApiResponse<Void> response = ApiResponse.error("ERR", "msg", "c");
        assertThat(response.getTimestamp()).isNotBlank();
    }
}
