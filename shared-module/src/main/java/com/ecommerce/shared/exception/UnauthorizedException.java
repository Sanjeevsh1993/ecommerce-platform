package com.ecommerce.shared.exception;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: web.xml security-constraint (auth-protected /admin/* pattern)
// BC References: BC-035 (admin role enforcement → HTTP 401/403)

/**
 * Thrown when a request lacks valid authentication or authorization.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Authentication required to access this resource");
    }
}
