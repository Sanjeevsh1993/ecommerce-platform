package com.ecommerce.user.controller;

// STRANGLER FIG - Phase: 6 - Domain: User / Customer Management
// Migrated from: DisplayCustomerSummaryCommand, CustomerMaintenance flow actions
// BC References: BC-001..012, BC-043 (ApiResponse), BC-046 (correlationId)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management — BC-001..012")
public class CustomerController {

    private final CustomerService customerService;

    // BC-001: customer summary (replaces DisplayCustomerSummaryCommand)
    @GetMapping("/{customerId}/summary")
    @Operation(summary = "Get customer summary — BC-001")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerSummaryDto>> getCustomerSummary(
            @PathVariable Long customerId,
            @RequestHeader(value = AppConstants.CORRELATION_ID_HEADER, required = false) String cid) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomerSummary(customerId), CorrelationIdUtils.fromMdc()));
    }

    // BC-002: list + search
    @GetMapping
    @Operation(summary = "List/search customers — BC-002")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> listCustomers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.listCustomers(query, pageable), CorrelationIdUtils.fromMdc()));
    }

    // BC-003: create customer
    @PostMapping
    @Operation(summary = "Create customer — BC-003")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                customerService.createCustomer(request), CorrelationIdUtils.fromMdc()));
    }

    // BC-004: update customer
    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer — BC-004")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.updateCustomer(customerId, request), CorrelationIdUtils.fromMdc()));
    }

    // BC-013: internal endpoint called by order-service Feign client for customer pre-population
    @GetMapping("/by-number/{customerNumber}/summary")
    @Operation(summary = "Get customer summary by customerNumber — used by order-service (BC-013)")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerSummaryDto>> getCustomerSummaryByNumber(
            @PathVariable String customerNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomerSummaryByNumber(customerNumber), CorrelationIdUtils.fromMdc()));
    }
}
