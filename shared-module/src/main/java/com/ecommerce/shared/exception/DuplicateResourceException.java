package com.ecommerce.shared.exception;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: No equivalent in old system (mock data, no uniqueness checks)
// BC References: BC-038 (registration — email must be unique → HTTP 409)

/**
 * Thrown when attempting to create a resource that already exists.
 * Maps to HTTP 409 Conflict.
 *
 * Example: registering with an email that already exists (BC-038).
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}
