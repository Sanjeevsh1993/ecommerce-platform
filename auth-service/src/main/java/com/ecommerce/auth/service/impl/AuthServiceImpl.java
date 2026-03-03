package com.ecommerce.auth.service.impl;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// Migrated from: session-based login in web.xml; CustomerBO.loadCustomer() for role resolution
// BC References: BC-034 (register/login), BC-035 (BCrypt), BC-036 (enabled check),
//                BC-037 (password reset), BC-038 (token rotation), BC-039 (7-day refresh expiry),
//                BC-040 (access token body), BC-041 (HTTP-only cookie), BC-042 (JWT claims)

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.shared.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // BC-039: 7 days (604800000ms)
    @Value("${app.jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    @Value("${app.jwt.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    // ── register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        // BC-034: duplicate email check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                // BC-035: BCrypt hash
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                // BC-034: default role for self-registration is ROLE_CUSTOMER
                .roles(Set.of("ROLE_CUSTOMER"))
                .enabled(true)
                .accountNonLocked(true)
                .build();

        user = userRepository.save(user);
        log.info("Registered new user: {}", user.getEmail());

        return issueTokens(user, response);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // BC-036: Spring Security handles enabled/locked checks
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        log.info("User logged in: {}", user.getEmail());
        return issueTokens(user, response);
    }

    // ── refresh-token ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // BC-041: read refresh token from HTTP-only cookie
        String rawToken = extractRefreshCookie(request);
        if (rawToken == null) {
            throw new UnauthorizedException("Refresh token cookie not found");
        }

        String tokenHash = sha256(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        // BC-038: check not revoked and not expired
        if (stored.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // BC-038: token rotation — revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        log.info("Refresh token rotated for user: {}", user.getEmail());
        return issueTokens(user, response);
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshCookie(request);
        if (rawToken != null) {
            String tokenHash = sha256(rawToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
                log.info("Refresh token revoked for user: {}", rt.getUser().getEmail());
            });
        }
        // BC-041: clear HTTP-only cookie
        clearRefreshCookie(response);
    }

    // ── forgot-password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // BC-037: silently no-op if email not found (prevent email enumeration)
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(Instant.now().plusSeconds(3600)); // 1 hour
            userRepository.save(user);
            // TODO BC-037: in production, email the token via MailService
            log.info("Password reset token generated for user: {}", user.getEmail());
        });
    }

    // ── reset-password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new BusinessRuleException("INVALID_RESET_TOKEN", "Password reset token is invalid or has expired"));

        // BC-037: token expiry check
        if (user.getPasswordResetTokenExpiry() == null ||
                user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new BusinessRuleException("EXPIRED_RESET_TOKEN", "Password reset token has expired");
        }

        // BC-035: hash the new password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Password reset completed for user: {}", user.getEmail());
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private AuthResponse issueTokens(User user, HttpServletResponse response) {
        // BC-040: access token in response body
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRoles());

        // BC-038: opaque refresh token stored as SHA-256 hash
        String rawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        String tokenHash = sha256(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        // BC-041: refresh token as HTTP-only, Secure, SameSite=Strict cookie
        setRefreshCookie(response, rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiryMs() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .build();
    }

    private void setRefreshCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie(refreshCookieName, rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenExpiryMs / 1000));
        // SameSite=Strict via header (Servlet API < 6 doesn't support setAttribute)
        response.addHeader("Set-Cookie",
                refreshCookieName + "=" + rawToken
                + "; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=" + (refreshTokenExpiryMs / 1000));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                refreshCookieName + "=; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=0");
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> refreshCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
