package com.ecommerce.catalog.controller;

// STRANGLER FIG - Phase: 6 - Domain: Catalog Management
// Migrated from: CatalogManagementFlowCommandExecutor → DisplayCatalogItem → maintainCatalogItem.jsp
// BC References: BC-031, BC-032, BC-043, BC-046

import com.ecommerce.catalog.dto.*;
import com.ecommerce.catalog.service.CatalogService;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.util.CorrelationIdUtils;
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
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Catalog item management — BC-031, BC-032")
public class CatalogController {

    private final CatalogService catalogService;

    // BC-031: display catalog item by ID
    @GetMapping("/items/{id}")
    @Operation(summary = "Get catalog item by ID — BC-031")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<CatalogItemDto>> getCatalogItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.getCatalogItem(id), CorrelationIdUtils.fromMdc()));
    }

    // BC-031: display catalog item by code
    @GetMapping("/items/code/{itemCode}")
    @Operation(summary = "Get catalog item by code — BC-031")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<CatalogItemDto>> getCatalogItemByCode(@PathVariable String itemCode) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.getCatalogItemByCode(itemCode), CorrelationIdUtils.fromMdc()));
    }

    // BC-031: list/search active items
    @GetMapping("/items")
    @Operation(summary = "List/search catalog items — BC-031")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<CatalogItemDto>>> listCatalogItems(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 5) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.listCatalogItems(query, pageable), CorrelationIdUtils.fromMdc()));
    }

    // BC-032: create catalog item (admin only)
    @PostMapping("/items")
    @Operation(summary = "Create catalog item — BC-032")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogItemDto>> createCatalogItem(
            @Valid @RequestBody CreateCatalogItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                catalogService.createCatalogItem(request), CorrelationIdUtils.fromMdc()));
    }

    // BC-032: update catalog item (admin only)
    @PutMapping("/items/{id}")
    @Operation(summary = "Update catalog item — BC-032")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogItemDto>> updateCatalogItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCatalogItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                catalogService.updateCatalogItem(id, request), CorrelationIdUtils.fromMdc()));
    }
}
