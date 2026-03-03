package com.ecommerce.auth.integration;

// STRANGLER FIG - Phase: 10 - Domain: Authentication — Integration Tests
// BC References: BC-034..042
// Uses TestContainers MySQL — same database engine as production

import com.ecommerce.auth.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("it")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIT {

    // Single shared container for all tests in this class (reuse = faster)
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ecommerce_auth")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "it-test@example.com";
    private static final String TEST_PASSWORD = "SecurePass1!";

    // BC-034: register new user
    @Test
    @Order(1)
    void register_newUser_returns201AndAccessToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Integration"); req.setLastName("Test");
        req.setEmail(TEST_EMAIL); req.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                // BC-034: default role is ROLE_CUSTOMER
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_CUSTOMER"));
    }

    // BC-035: duplicate email rejected with real DB
    @Test
    @Order(2)
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Dup"); req.setLastName("User");
        req.setEmail(TEST_EMAIL); req.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    // BC-034: login with correct credentials
    @Test
    @Order(3)
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL); req.setPassword(TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        // BC-041: refresh token cookie must be set
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("refresh_token");
        assertThat(setCookie).contains("HttpOnly");
    }

    // BC-034: login with wrong password
    @Test
    @Order(4)
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(TEST_EMAIL); req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // BC-037: forgot password for unknown email → silent 200 (no enumeration)
    @Test
    @Order(5)
    void forgotPassword_unknownEmail_returns200Silently() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("nobody@nowhere.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // BC-038: refresh token rotation with real DB
    @Test
    @Order(6)
    void refreshToken_validCookie_returnsNewAccessToken() throws Exception {
        // First login to get the refresh token cookie
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(TEST_EMAIL); loginReq.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).isNotNull();

        // Extract refresh_token value from Set-Cookie header
        String refreshToken = extractCookieValue(setCookie, "refresh_token");

        // Now call refresh-token with the cookie
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("Cookie", "refresh_token=" + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // BC-038: logout revokes refresh token
    @Test
    @Order(7)
    void logout_revokesRefreshToken() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(TEST_EMAIL); loginReq.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String refreshToken = extractCookieValue(setCookie, "refresh_token");

        // Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Cookie", "refresh_token=" + refreshToken))
                .andExpect(status().isOk());

        // BC-038: using revoked token should now fail
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header("Cookie", "refresh_token=" + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    private String extractCookieValue(String setCookieHeader, String cookieName) {
        if (setCookieHeader == null) return null;
        for (String part : setCookieHeader.split(";")) {
            part = part.trim();
            if (part.startsWith(cookieName + "=")) {
                return part.substring((cookieName + "=").length());
            }
        }
        return null;
    }
}
