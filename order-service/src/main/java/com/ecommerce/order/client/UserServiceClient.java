package com.ecommerce.order.client;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// BC References: BC-013 (customer pre-population when adding order history)
// Called when order-service needs to validate/pre-populate customer data

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.ecommerce.shared.response.ApiResponse;

@FeignClient(name = "user-service", url = "${app.services.user-service-url:http://localhost:8082}")
public interface UserServiceClient {

    @GetMapping("/api/v1/customers/{customerId}/summary")
    ApiResponse<CustomerSummaryClientDto> getCustomerSummary(@PathVariable Long customerId);

    @GetMapping("/api/v1/customers/by-number/{customerNumber}/summary")
    ApiResponse<CustomerSummaryClientDto> getCustomerSummaryByNumber(@PathVariable String customerNumber);
}
