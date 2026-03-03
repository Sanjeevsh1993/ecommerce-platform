package com.ecommerce.shared.handler;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: ControllerServlet catch blocks, web.xml error-page definitions
// BC References: BC-043 (standard error response), BC-044 (FlowException handling),
//                BC-045 (BusinessDelegateException), BC-046 (correlationId in response)

import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.shared.exception.UnauthorizedException;
import com.ecommerce.shared.exception.ValidationException;
import com.ecommerce.shared.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for ALL microservices (imported via shared-module).
 *
 * Produces the standard error response format (BC-043):
 * {
 *   "success": false,
 *   "timestamp": "...",
 *   "correlationId": "uuid",
 *   "error": { "code": "...", "message": "...", "details": [...] }
 * }
 *
 * Migrated from: ControllerServlet.processRequest() catch blocks (both FlowException
 * and generic Exception were forwarded to /error.jsp with errorMessage attribute).
 * New system returns structured JSON instead of JSP redirect.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("[{}] Resource not found: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage(), correlationId()));
    }

    // ── 409 Conflict ─────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("[{}] Duplicate resource: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_RESOURCE", ex.getMessage(), correlationId()));
    }

    // ── 422 Unprocessable Entity (business rule / validation) ────────────────

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        log.warn("[{}] Business rule violation [{}]: {}", correlationId(), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), correlationId()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.warn("[{}] Validation error: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("VALIDATION_ERROR", ex.getMessage(),
                        ex.getValidationErrors(), correlationId()));
    }

    // ── 400 Bad Request (Spring @Valid failures) ─────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        log.warn("[{}] Request validation failed: {}", correlationId(), fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_REQUEST",
                        "Request validation failed",
                        fieldErrors,
                        correlationId()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {
        List<String> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        log.warn("[{}] Constraint violations: {}", correlationId(), violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("CONSTRAINT_VIOLATION",
                        "Request constraint violation",
                        violations,
                        correlationId()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        log.warn("[{}] {}", correlationId(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("MISSING_PARAMETER", message, correlationId()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' has invalid value '%s'",
                ex.getName(), ex.getValue());
        log.warn("[{}] {}", correlationId(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_PARAMETER_TYPE", message, correlationId()));
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("[{}] Unauthorized: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", ex.getMessage(), correlationId()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("[{}] Authentication failed: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTHENTICATION_FAILED", ex.getMessage(), correlationId()));
    }

    // ── 403 Forbidden ────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[{}] Access denied: {}", correlationId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED",
                        "You do not have permission to access this resource",
                        correlationId()));
    }

    // ── 500 Internal Server Error (catch-all) ────────────────────────────────
    // Migrated from: ControllerServlet catch(Exception e) → "An unexpected error occurred: " + message

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("[{}] Unexpected error: {}", correlationId(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        correlationId()));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String correlationId() {
        String id = MDC.get("correlationId");
        return id != null ? id : "unknown";
    }
}
