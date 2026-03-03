package com.ecommerce.auth.dto;

// BC References: BC-040 (access token in body), BC-041 (refresh token in HTTP-only cookie)

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class AuthResponse {

    // BC-040: access token (15-min JWT) returned in response body
    private String accessToken;

    // BC-040: token type is always "Bearer"
    @Builder.Default
    private String tokenType = "Bearer";

    // BC-040: expiry hint for client
    private long expiresIn;

    // User info for client-side rendering (no password, no hash)
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;

    // BC-034: roles for client-side menu rendering
    private Set<String> roles;

    // Note: refresh token is NOT in this body — it is set as HTTP-only cookie by the controller (BC-041)
}
