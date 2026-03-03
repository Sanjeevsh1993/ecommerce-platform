package com.ecommerce.shared.exception;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: FlowException, BusinessDelegateException (business logic failures)
// BC References: BC-044 (FlowException → HTTP 422), BC-045 (BusinessDelegateException propagation)

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * Examples:
 * - Saving order history with PRODUCT/ORDER type when only CUSTOMER is supported (BC-014)
 * - Work queue fields missing when createWorkQueue=true (BC-017)
 */
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
