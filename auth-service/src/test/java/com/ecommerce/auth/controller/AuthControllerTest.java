package com.ecommerce.auth.controller;

// BC References: BC-034, BC-038, BC-043 (ApiResponse wrapper)

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(com.ecommerce.auth.config.SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean com.ecommerce.auth.security.JwtAuthFilter jwtAuthFilter;
    @MockBean com.ecommerce.auth.security.UserDetailsServiceImpl userDetailsService;

    // All auth endpoints are permitAll() — filter must forward the chain
    @BeforeEach
    void setupFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private AuthResponse sampleAuthResponse() {
        return AuthResponse.builder()
                .accessToken("test-jwt")
                .tokenType("Bearer")
                .expiresIn(900L)
                .userId(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of("ROLE_CUSTOMER"))
                .build();
    }

    @Test
    void register_validRequest_returns201WithAccessToken() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane@example.com");
        req.setPassword("SecurePass1!");

        when(authService.register(any(RegisterRequest.class), any(HttpServletResponse.class)))
                .thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void register_blankEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("");
        req.setPassword("SecurePass1!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validRequest_returns200WithToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("password");

        when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
                .thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    void refreshToken_callsService_returns200() throws Exception {
        when(authService.refreshToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(sampleAuthResponse());

        mockMvc.perform(post("/api/v1/auth/refresh-token").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void logout_callsService_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void forgotPassword_validEmail_returns200() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("user@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPassword_validRequest_returns200() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("reset-token-123");
        req.setNewPassword("NewSecure1!");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
