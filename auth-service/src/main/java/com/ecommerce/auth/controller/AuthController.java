package com.ecommerce.auth.controller;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// Migrated from: web.xml FORM-based login → /control?flow=... servlet
// BC References: BC-034 (register/login endpoints), BC-037 (password reset),
//                BC-038 (refresh/logout), BC-043 (ApiResponse wrapper), BC-046 (correlation ID)

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, token refresh, logout, password reset")
public class AuthController {

    private final AuthService authService;

    // ── POST /api/v1/auth/register ────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user account (ROLE_CUSTOMER by default)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response,
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        AuthResponse auth = authService.register(request, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(auth, CorrelationIdUtils.fromMdc()));
    }

    // ── POST /api/v1/auth/login ───────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email + password; returns access token + sets refresh cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response,
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {

        AuthResponse auth = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success(auth, CorrelationIdUtils.fromMdc()));
    }

    // ── POST /api/v1/auth/refresh-token ──────────────────────────────────────

    @PostMapping("/refresh-token")
    @Operation(summary = "Rotate refresh token (reads HTTP-only cookie); returns new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.refreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success(auth, CorrelationIdUtils.fromMdc()));
    }

    // ── POST /api/v1/auth/logout ──────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token and clear cookie")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success(null, CorrelationIdUtils.fromMdc()));
    }

    // ── POST /api/v1/auth/forgot-password ────────────────────────────────────

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset — sends reset token (silently no-ops for unknown emails)")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, CorrelationIdUtils.fromMdc()));
    }

    // ── POST /api/v1/auth/reset-password ─────────────────────────────────────

    @PostMapping("/reset-password")
    @Operation(summary = "Complete password reset using token from email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, CorrelationIdUtils.fromMdc()));
    }
}
