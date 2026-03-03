package com.ecommerce.order.client;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// BC References: BC-013 (customer pre-population when adding order history)
// Replaces the Feign client with a WebClient-based implementation.
// WebClient is used in blocking mode (.block()) since order-service is a servlet (MVC) app.

import com.ecommerce.shared.response.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient userServiceWebClient;

    // BC-013: fetch customer summary by ID to pre-populate order history list
    @CircuitBreaker(name = "user-service", fallbackMethod = "getCustomerSummaryFallback")
    public CustomerSummaryClientDto getCustomerSummary(Long customerId) {
        return userServiceWebClient.get()
                .uri("/api/v1/customers/{customerId}/summary", customerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerSummaryClientDto>>() {})
                .map(ApiResponse::getData)
                .block();
    }

    // BC-013: fetch customer summary by customer number
    @CircuitBreaker(name = "user-service", fallbackMethod = "getCustomerSummaryByNumberFallback")
    public CustomerSummaryClientDto getCustomerSummaryByNumber(String customerNumber) {
        return userServiceWebClient.get()
                .uri("/api/v1/customers/by-number/{customerNumber}/summary", customerNumber)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerSummaryClientDto>>() {})
                .map(ApiResponse::getData)
                .block();
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
