package com.ecommerce.shared.exception;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No direct equivalent — old system had no explicit field validation layer
// BC References: BC-043 (field validation errors → details array in error response)

import java.util.List;

/**
 * Thrown when request data fails business validation.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Carries a list of field-level validation messages for the
 * error response "details" array (BC-043).
 */
public class ValidationException extends RuntimeException {

    private final List<String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
