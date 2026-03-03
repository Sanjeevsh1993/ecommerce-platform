package com.ecommerce.user.service;

// BC References: BC-001..012

import com.ecommerce.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    // BC-001: customer summary for header + summary panel
    CustomerSummaryDto getCustomerSummary(Long customerId);
    // BC-002: paginated list + search
    Page<CustomerDto> listCustomers(String query, Pageable pageable);
    // BC-003: create
    CustomerDto createCustomer(CreateCustomerRequest request);
    // BC-004: update
    CustomerDto updateCustomer(Long customerId, UpdateCustomerRequest request);
    // internal: used by order-service via Feign for customer pre-population (BC-013)
    CustomerSummaryDto getCustomerSummaryByNumber(String customerNumber);
}
