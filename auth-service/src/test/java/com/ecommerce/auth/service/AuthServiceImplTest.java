package com.ecommerce.auth.service;

// BC References: BC-034..BC-042

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.RefreshToken;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtService;
import com.ecommerce.auth.service.impl.AuthServiceImpl;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock HttpServletResponse httpServletResponse;
    @Mock HttpServletRequest httpServletRequest;

    @InjectMocks
    AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 604800000L);
        ReflectionTestUtils.setField(authService, "refreshCookieName", "refresh_token");
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_newUser_returnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPassword("SecurePass1!");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass1!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 1L);
            return u;
        });
        when(jwtService.generateAccessToken(anyString(), anyCollection())).thenReturn("access-jwt");
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(request, httpServletResponse);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getAccessToken()).isEqualTo("access-jwt");
        assertThat(response.getRoles()).containsExactly("ROLE_CUSTOMER");
        // BC-035: password was encoded
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, httpServletResponse))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password");

        User user = User.builder()
                .email("admin@example.com")
                .passwordHash("hash")
                .roles(Set.of("ROLE_ADMIN"))
                .enabled(true)
                .accountNonLocked(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 10L);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(anyString(), anyCollection())).thenReturn("jwt-token");
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.login(request, httpServletResponse);

        assertThat(response.getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getRoles()).containsExactly("ROLE_ADMIN");
        verify(authenticationManager).authenticate(any());
    }

    // ── refresh token ─────────────────────────────────────────────────────────

    @Test
    void refreshToken_validCookie_rotatesTokenAndReturnsNewResponse() {
        String rawToken = "raw-refresh-token-value";
        User user = User.builder()
                .email("user@example.com")
                .roles(Set.of("ROLE_CUSTOMER"))
                .enabled(true)
                .accountNonLocked(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 5L);

        RefreshToken stored = RefreshToken.builder()
                .tokenHash(sha256(rawToken))
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(httpServletRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refresh_token", rawToken)});
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(anyString(), anyCollection())).thenReturn("new-jwt");
        when(jwtService.getAccessTokenExpiryMs()).thenReturn(900000L);

        AuthResponse response = authService.refreshToken(httpServletRequest, httpServletResponse);

        assertThat(response.getAccessToken()).isEqualTo("new-jwt");
        // BC-038: old token must be revoked
        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    void refreshToken_missingCookie_throwsUnauthorizedException() {
        when(httpServletRequest.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest, httpServletResponse))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshToken_expiredToken_throwsUnauthorizedException() {
        String rawToken = "expired-token";
        User user = User.builder().email("u@e.com").roles(Set.of("ROLE_CUSTOMER")).build();
        RefreshToken stored = RefreshToken.builder()
                .tokenHash(sha256(rawToken))
                .user(user)
                .expiresAt(Instant.now().minusSeconds(1))  // expired
                .revoked(false)
                .build();

        when(httpServletRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refresh_token", rawToken)});
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest, httpServletResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refreshToken_revokedToken_throwsUnauthorizedException() {
        String rawToken = "revoked-token";
        User user = User.builder().email("u@e.com").roles(Set.of("ROLE_CUSTOMER")).build();
        RefreshToken stored = RefreshToken.builder()
                .tokenHash(sha256(rawToken))
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(true)   // already revoked
                .build();

        when(httpServletRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refresh_token", rawToken)});
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest, httpServletResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_validCookie_revokesToken() {
        String rawToken = "logout-token";
        User user = User.builder().email("u@e.com").build();
        RefreshToken stored = RefreshToken.builder()
                .tokenHash(sha256(rawToken))
                .user(user)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(httpServletRequest.getCookies()).thenReturn(
                new Cookie[]{new Cookie("refresh_token", rawToken)});
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(httpServletRequest, httpServletResponse);

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logout_noCookie_doesNotThrow() {
        when(httpServletRequest.getCookies()).thenReturn(null);
        authService.logout(httpServletRequest, httpServletResponse); // should not throw
    }

    // ── forgot-password ───────────────────────────────────────────────────────

    @Test
    void forgotPassword_knownEmail_setsResetToken() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("known@example.com");

        User user = User.builder().email("known@example.com").build();
        when(userRepository.findByEmail("known@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordResetToken()).isNotBlank();
        assertThat(captor.getValue().getPasswordResetTokenExpiry()).isAfter(Instant.now());
    }

    @Test
    void forgotPassword_unknownEmail_silentlyNoOps() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(request);  // must NOT throw — prevent email enumeration
        verify(userRepository, never()).save(any());
    }

    // ── reset-password ────────────────────────────────────────────────────────

    @Test
    void resetPassword_validToken_updatesPasswordHash() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("NewSecure1!");

        User user = User.builder()
                .email("user@example.com")
                .passwordResetToken("valid-reset-token")
                .passwordResetTokenExpiry(Instant.now().plusSeconds(3600))
                .build();

        when(userRepository.findByPasswordResetToken("valid-reset-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewSecure1!")).thenReturn("new-hash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword(request);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getPasswordResetToken()).isNull();
        assertThat(user.getPasswordResetTokenExpiry()).isNull();
    }

    @Test
    void resetPassword_expiredToken_throwsBusinessRuleException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNewPassword("NewPass1!");

        User user = User.builder()
                .passwordResetToken("expired-token")
                .passwordResetTokenExpiry(Instant.now().minusSeconds(1))  // expired
                .build();

        when(userRepository.findByPasswordResetToken("expired-token")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_invalidToken_throwsBusinessRuleException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setNewPassword("NewPass1!");

        when(userRepository.findByPasswordResetToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ── helper — mirrors AuthServiceImpl.sha256 ───────────────────────────────

    private String sha256(String input) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
