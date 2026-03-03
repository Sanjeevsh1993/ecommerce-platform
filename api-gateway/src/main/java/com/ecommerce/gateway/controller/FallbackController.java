package com.ecommerce.gateway.controller;

// STRANGLER FIG - Phase: 5 - Domain: Gateway / Cross-Cutting
// BC References: BC-043 (ApiResponse wrapper), BC-050 (circuit breaker fallback)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * BC-050: Circuit breaker fallback endpoints.
 * When a downstream service is unavailable, Spring Cloud Gateway forwards here.
 * Response follows the standard ApiResponse<Void> error format (BC-043).
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/auth")
    public Mono<ResponseEntity<ApiResponse<Void>>> authFallback(
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {
        return fallbackResponse("auth-service", correlationId);
    }

    @RequestMapping("/user")
    public Mono<ResponseEntity<ApiResponse<Void>>> userFallback(
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {
        return fallbackResponse("user-service", correlationId);
    }

    @RequestMapping("/order")
    public Mono<ResponseEntity<ApiResponse<Void>>> orderFallback(
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {
        return fallbackResponse("order-service", correlationId);
    }

    @RequestMapping("/catalog")
    public Mono<ResponseEntity<ApiResponse<Void>>> catalogFallback(
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String correlationId) {
        return fallbackResponse("catalog-service", correlationId);
    }

    private Mono<ResponseEntity<ApiResponse<Void>>> fallbackResponse(String service, String correlationId) {
        String cid = CorrelationIdUtils.resolveOrGenerate(correlationId);
        log.warn("Circuit breaker fallback triggered for service={} correlationId={}", service, cid);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "SERVICE_UNAVAILABLE",
                        service + " is temporarily unavailable. Please try again later.",
                        cid)));
    }
}
