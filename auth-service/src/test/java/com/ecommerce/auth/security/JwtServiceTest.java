package com.ecommerce.auth.security;

// BC References: BC-040 (access token), BC-042 (JWT claims: sub=email, roles)

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // Minimum 32-char key required for HS256
    private static final String TEST_SECRET = "test-secret-key-that-is-32-chars-long!!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 900000L);
    }

    @Test
    void generateAccessToken_returnsNonBlankJwt() {
        String token = jwtService.generateAccessToken("user@example.com", Set.of("ROLE_CUSTOMER"));
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractEmail_returnsSubject() {
        String token = jwtService.generateAccessToken("jane@example.com", List.of("ROLE_CUSTOMER"));
        assertThat(jwtService.extractEmail(token)).isEqualTo("jane@example.com");
    }

    @Test
    void isTokenValid_withMatchingUser_returnsTrue() {
        String token = jwtService.generateAccessToken("admin@example.com", List.of("ROLE_ADMIN"));
        var userDetails = User.withUsername("admin@example.com")
                .password("hashed")
                .roles("ADMIN")
                .build();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withWrongUser_returnsFalse() {
        String token = jwtService.generateAccessToken("a@example.com", List.of("ROLE_CUSTOMER"));
        var userDetails = User.withUsername("b@example.com").password("x").roles("CUSTOMER").build();
        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        // 1ms expiry — token expires immediately
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMs", 1L);
        String token = jwtService.generateAccessToken("u@e.com", List.of("ROLE_CUSTOMER"));

        // Give it a moment to expire
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    void getAccessTokenExpiryMs_returnsConfiguredValue() {
        assertThat(jwtService.getAccessTokenExpiryMs()).isEqualTo(900000L);
    }
}
