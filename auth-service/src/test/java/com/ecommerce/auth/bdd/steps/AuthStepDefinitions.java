package com.ecommerce.auth.bdd.steps;

// BC References: BC-034..042

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RefreshTokenRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@CucumberContextConfiguration
public class AuthStepDefinitions {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserRepository userRepository;
    @MockBean RefreshTokenRepository refreshTokenRepository;
    @MockBean PasswordEncoder passwordEncoder;

    private MvcResult lastResult;
    private String pendingEmail;
    private String pendingPassword;

    @Before
    public void resetMocks() {
        reset(userRepository, refreshTokenRepository, passwordEncoder);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPasswordResetToken(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 99L);
            return u;
        });
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("a registration request with email {string} and password {string}")
    public void aRegistrationRequestWith(String email, String password) {
        pendingEmail = email;
        pendingPassword = password;
    }

    @Given("a user already exists with email {string}")
    public void aUserAlreadyExistsWith(String email) {
        when(userRepository.existsByEmail(email)).thenReturn(true);
    }

    @Given("a registered user with email {string} password {string} and role {string}")
    public void aRegisteredUserWith(String email, String password, String role) {
        User user = User.builder()
                .email(email).firstName("Test").lastName("User")
                .passwordHash("hashed-" + password)
                .roles(Set.of(role))
                .enabled(true).accountNonLocked(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(password), anyString())).thenReturn(true);
    }

    @Given("a password reset token {string} that is expired")
    public void aPasswordResetTokenThatIsExpired(String token) {
        User user = User.builder()
                .email("user@example.com").firstName("T").lastName("U")
                .passwordHash("hash")
                .passwordResetToken(token)
                .passwordResetTokenExpiry(Instant.now().minusSeconds(1)) // expired
                .roles(Set.of("ROLE_CUSTOMER"))
                .enabled(true).accountNonLocked(true)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(user));
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("the user submits the registration")
    public void theUserSubmitsRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Test"); req.setLastName("User");
        req.setEmail(pendingEmail); req.setPassword(pendingPassword);

        lastResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @When("the user logs in with email {string} and password {string}")
    public void theUserLogsIn(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email); req.setPassword(password);

        lastResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @When("the user requests a password reset for {string}")
    public void theUserRequestsPasswordReset(String email) throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail(email);

        lastResult = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    @When("the user resets password with token {string} and new password {string}")
    public void theUserResetsPassword(String token, String newPassword) throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken(token); req.setNewPassword(newPassword);

        lastResult = mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(lastResult.getResponse().getStatus()).isEqualTo(status);
    }

    @Then("the response contains an access token")
    public void theResponseContainsAccessToken() throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains("accessToken");
    }

    @Then("the response contains role {string}")
    public void theResponseContainsRole(String role) throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains(role);
    }

    @Then("the error code is {string}")
    public void theErrorCodeIs(String code) throws Exception {
        assertThat(lastResult.getResponse().getContentAsString()).contains(code);
    }
}
