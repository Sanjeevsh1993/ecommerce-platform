package com.ecommerce.shared.exception;

// BC References: BC-043, BC-044, BC-045

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    // ── ResourceNotFoundException ─────────────────────────────────────────────

    @Test
    void resourceNotFoundException_withFields_formatsMessage() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Customer", "id", 123L);

        assertThat(ex.getMessage()).contains("Customer", "id", "123");
        assertThat(ex.getResourceName()).isEqualTo("Customer");
        assertThat(ex.getFieldName()).isEqualTo("id");
        assertThat(ex.getFieldValue()).isEqualTo(123L);
    }

    @Test
    void resourceNotFoundException_withMessage_preservesMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Custom message");
        assertThat(ex.getMessage()).isEqualTo("Custom message");
        assertThat(ex.getResourceName()).isNull();
    }

    // ── DuplicateResourceException ────────────────────────────────────────────

    @Test
    void duplicateResourceException_formatsMessage() {
        DuplicateResourceException ex =
                new DuplicateResourceException("User", "email", "test@example.com");

        assertThat(ex.getMessage()).contains("User", "email", "test@example.com");
        assertThat(ex.getFieldValue()).isEqualTo("test@example.com");
    }

    // ── ValidationException ───────────────────────────────────────────────────

    @Test
    void validationException_withErrors_preservesAllErrors() {
        List<String> errors = List.of("error1", "error2");
        ValidationException ex = new ValidationException("Validation failed", errors);

        assertThat(ex.getMessage()).isEqualTo("Validation failed");
        assertThat(ex.getValidationErrors()).containsExactly("error1", "error2");
    }

    @Test
    void validationException_singleMessage_wrapsInList() {
        ValidationException ex = new ValidationException("Single error");
        assertThat(ex.getValidationErrors()).containsExactly("Single error");
    }

    // ── BusinessRuleException ─────────────────────────────────────────────────

    @Test
    void businessRuleException_withErrorCode_preservesCode() {
        BusinessRuleException ex =
                new BusinessRuleException("INVALID_ORDER_TYPE", "Only CUSTOMER type allowed");

        assertThat(ex.getErrorCode()).isEqualTo("INVALID_ORDER_TYPE");
        assertThat(ex.getMessage()).contains("CUSTOMER");
    }

    @Test
    void businessRuleException_withoutCode_usesDefault() {
        BusinessRuleException ex = new BusinessRuleException("Some rule violated");
        assertThat(ex.getErrorCode()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    // ── UnauthorizedException ─────────────────────────────────────────────────

    @Test
    void unauthorizedException_defaultMessage() {
        UnauthorizedException ex = new UnauthorizedException();
        assertThat(ex.getMessage()).contains("Authentication required");
    }

    @Test
    void unauthorizedException_customMessage() {
        UnauthorizedException ex = new UnauthorizedException("Token expired");
        assertThat(ex.getMessage()).isEqualTo("Token expired");
    }
}
