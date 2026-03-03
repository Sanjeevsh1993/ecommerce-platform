package com.ecommerce.shared.exception;

// BC References: BC-043, BC-044, BC-045

import com.ecommerce.shared.handler.GlobalExceptionHandler;
import com.ecommerce.shared.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Customer", "id", 99L);

        ResponseEntity<ApiResponse<Void>> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError().getCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void handleDuplicateResource_returns409() {
        DuplicateResourceException ex =
                new DuplicateResourceException("User", "email", "test@example.com");

        ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateResource(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError().getCode()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    void handleBusinessRule_returns422() {
        BusinessRuleException ex =
                new BusinessRuleException("INVALID_TYPE", "Only CUSTOMER type is allowed");

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessRule(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getError().getCode()).isEqualTo("INVALID_TYPE");
    }

    @Test
    void handleValidation_returns422() {
        ValidationException ex = new ValidationException("Invalid input");

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getError().getCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void handleUnauthorized_returns401() {
        UnauthorizedException ex = new UnauthorizedException("Token expired");

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError().getCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError().getCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("Unexpected failure");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError().getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        // BC-044: must NOT leak internal message to client
        assertThat(response.getBody().getError().getMessage())
                .doesNotContain("Unexpected failure");
    }

    @Test
    void handleAuthenticationException_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError().getCode()).isEqualTo("AUTHENTICATION_FAILED");
    }
}
