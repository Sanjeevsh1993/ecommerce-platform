package com.ecommerce.auth.dto;

// BC References: BC-038 (refresh token rotation — token read from cookie, not body)

import lombok.Data;

/**
 * Used when the client explicitly sends the refresh token in the request body.
 * In the primary flow the controller reads it from the HTTP-only cookie instead.
 */
@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
