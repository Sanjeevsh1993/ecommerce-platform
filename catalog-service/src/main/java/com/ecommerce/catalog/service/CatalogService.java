package com.ecommerce.catalog.service;

// BC References: BC-031, BC-032

import com.ecommerce.catalog.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogService {
    // BC-031: display/view item
    CatalogItemDto getCatalogItem(Long id);
    CatalogItemDto getCatalogItemByCode(String itemCode);
    // BC-031: list/search
    Page<CatalogItemDto> listCatalogItems(String query, Pageable pageable);
    // BC-032: create
    CatalogItemDto createCatalogItem(CreateCatalogItemRequest request);
    // BC-032: update
    CatalogItemDto updateCatalogItem(Long id, UpdateCatalogItemRequest request);
}
