package com.ecommerce.order.controller;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: CustomerMaintenance flow actions seq 71 (add), 73 (WQ add), 74 (update), 75 (view), 76 (maintain list)
// BC References: BC-013..025, BC-043, BC-046, BC-056

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.service.OrderHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order-history")
@RequiredArgsConstructor
@Tag(name = "Order History", description = "Order history management — BC-013..025")
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    // BC-013/020: list customer order history (with external toggle)
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "List customer order history — BC-013, BC-020")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryListResponse>> listOrderHistory(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "false") boolean includeExternal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderHistoryService.listOrderHistory(customerId, includeExternal),
                CorrelationIdUtils.fromMdc()));
    }

    // BC-019: view single entry
    @GetMapping("/{orderHistoryId}")
    @Operation(summary = "Get single order history entry — BC-019")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryDto>> getOrderHistory(
            @PathVariable Long orderHistoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderHistoryService.getOrderHistory(orderHistoryId),
                CorrelationIdUtils.fromMdc()));
    }

    // BC-014/015: regular add (navigationHint driven by maintainScreen)
    @PostMapping
    @Operation(summary = "Add order history entry — BC-014 (to customer summary) / BC-015 (to maintain list)")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryDto>> addOrderHistory(
            @Valid @RequestBody CreateOrderHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                orderHistoryService.addOrderHistory(request),
                CorrelationIdUtils.fromMdc()));
    }

    // BC-016/017: work queue add (distinct from regular add — workQueueDisabled=false)
    @PostMapping("/work-queue")
    @Operation(summary = "Add order history with work queue — BC-016, BC-017")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryDto>> addOrderHistoryWithWorkQueue(
            @Valid @RequestBody CreateOrderHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                orderHistoryService.addOrderHistoryWithWorkQueue(request),
                CorrelationIdUtils.fromMdc()));
    }

    // BC-018: update
    @PutMapping("/{orderHistoryId}")
    @Operation(summary = "Update order history entry — BC-018")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderHistoryDto>> updateOrderHistory(
            @PathVariable Long orderHistoryId,
            @Valid @RequestBody UpdateOrderHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                orderHistoryService.updateOrderHistory(orderHistoryId, request),
                CorrelationIdUtils.fromMdc()));
    }
}
