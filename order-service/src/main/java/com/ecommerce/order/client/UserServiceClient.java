package com.ecommerce.order.client;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// BC References: BC-013 (customer pre-population when adding order history)
// Uses RestClient (Spring 6.1) — synchronous, non-blocking-thread-friendly for MVC apps.
// No .block() needed; RestClient is designed for servlet/MVC contexts.

import com.ecommerce.shared.response.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient userServiceRestClient;

    // BC-013: fetch customer summary by ID to pre-populate order history list
    @CircuitBreaker(name = "user-service", fallbackMethod = "getCustomerSummaryFallback")
    public CustomerSummaryClientDto getCustomerSummary(Long customerId) {
        return userServiceRestClient.get()
                .uri("/api/v1/customers/{customerId}/summary", customerId)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<CustomerSummaryClientDto>>() {})
                .getData();
    }

    // BC-013: fetch customer summary by customer number
    @CircuitBreaker(name = "user-service", fallbackMethod = "getCustomerSummaryByNumberFallback")
    public CustomerSummaryClientDto getCustomerSummaryByNumber(String customerNumber) {
        return userServiceRestClient.get()
                .uri("/api/v1/customers/by-number/{customerNumber}/summary", customerNumber)
                .retrieve()
                .body(new ParameterizedTypeReference<ApiResponse<CustomerSummaryClientDto>>() {})
                .getData();
    }

    private CustomerSummaryClientDto getCustomerSummaryFallback(Long customerId, Throwable t) {
        log.warn("user-service unavailable, returning empty summary for customerId={}: {}", customerId, t.getMessage());
        return null;
    }

    private CustomerSummaryClientDto getCustomerSummaryByNumberFallback(String customerNumber, Throwable t) {
        log.warn("user-service unavailable, returning empty summary for customerNumber={}: {}", customerNumber, t.getMessage());
        return null;
    }
}
