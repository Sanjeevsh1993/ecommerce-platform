package com.ecommerce.auth.service;

// BC References: BC-034..BC-042

import com.ecommerce.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // BC-034: register new user with ROLE_CUSTOMER by default
    AuthResponse register(RegisterRequest request, HttpServletResponse response);

    // BC-034: authenticate and issue access + refresh tokens
    AuthResponse login(LoginRequest request, HttpServletResponse response);

    // BC-038: rotate refresh token; old token revoked, new token issued
    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);

    // BC-038: revoke all refresh tokens for user; clear cookies
    void logout(HttpServletRequest request, HttpServletResponse response);

    // BC-037: initiate password reset — generate token and (in prod) email it
    void forgotPassword(ForgotPasswordRequest request);

    // BC-037: validate reset token and set new password
    void resetPassword(ResetPasswordRequest request);
}
