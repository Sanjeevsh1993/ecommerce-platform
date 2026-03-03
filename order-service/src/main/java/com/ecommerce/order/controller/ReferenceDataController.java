package com.ecommerce.order.controller;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistoryTag.java hardcoded dropdown data
// BC References: BC-026 (ref data endpoints), BC-027 (WQ types), BC-028 (WQ reasons, non-sequential IDs), BC-029 (order types)

import com.ecommerce.order.constants.*;
import com.ecommerce.order.dto.ReferenceDataDto;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reference-data")
@RequiredArgsConstructor
@Tag(name = "Reference Data", description = "Order types, work queue types and reasons — BC-026..029")
public class ReferenceDataController {

    // BC-029: order entry types (1-5)
    @GetMapping("/order-entry-types")
    @Operation(summary = "Get all order entry types — BC-029")
    public ResponseEntity<ApiResponse<List<ReferenceDataDto>>> getOrderEntryTypes() {
        List<ReferenceDataDto> data = Arrays.stream(OrderEntryType.values())
                .map(t -> ReferenceDataDto.builder().id(t.getId()).name(t.getDisplayName()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data, CorrelationIdUtils.fromMdc()));
    }

    // BC-027: work queue types (1-5)
    @GetMapping("/work-queue-types")
    @Operation(summary = "Get all work queue types — BC-027")
    public ResponseEntity<ApiResponse<List<ReferenceDataDto>>> getWorkQueueTypes() {
        List<ReferenceDataDto> data = Arrays.stream(WorkQueueType.values())
                .map(t -> ReferenceDataDto.builder().id(t.getId()).name(t.getDisplayName()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data, CorrelationIdUtils.fromMdc()));
    }

    // BC-028: work queue reasons — CRITICAL: IDs are non-sequential (101-103, 201-202)
    @GetMapping("/work-queue-reasons")
    @Operation(summary = "Get all work queue reasons — BC-028 (non-sequential IDs 101-103, 201-202)")
    public ResponseEntity<ApiResponse<List<ReferenceDataDto>>> getWorkQueueReasons() {
        List<ReferenceDataDto> data = Arrays.stream(WorkQueueReason.values())
                .map(r -> ReferenceDataDto.builder().id(r.getId()).name(r.getDisplayName()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data, CorrelationIdUtils.fromMdc()));
    }
}
