package com.ecommerce.catalog.service.impl;

// STRANGLER FIG - Phase: 6 - Domain: Catalog Management
// Migrated from: CatalogManagementFlowCommandExecutor → DisplayCatalogItem
// BC References: BC-031, BC-032

import com.ecommerce.catalog.dto.*;
import com.ecommerce.catalog.entity.CatalogItem;
import com.ecommerce.catalog.mapper.CatalogItemMapper;
import com.ecommerce.catalog.repository.CatalogItemRepository;
import com.ecommerce.catalog.service.CatalogService;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CatalogItemRepository catalogItemRepository;
    private final CatalogItemMapper catalogItemMapper;

    @Override
    @Transactional(readOnly = true)
    public CatalogItemDto getCatalogItem(Long id) {
        return catalogItemMapper.toDto(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogItemDto getCatalogItemByCode(String itemCode) {
        CatalogItem item = catalogItemRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("CatalogItem", "itemCode", itemCode));
        return catalogItemMapper.toDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogItemDto> listCatalogItems(String query, Pageable pageable) {
        return catalogItemRepository.searchActive(query, pageable).map(catalogItemMapper::toDto);
    }

    @Override
    @Transactional
    public CatalogItemDto createCatalogItem(CreateCatalogItemRequest request) {
        if (catalogItemRepository.existsByItemCode(request.getItemCode())) {
            throw new DuplicateResourceException("CatalogItem", "itemCode", request.getItemCode());
        }
        CatalogItem item = catalogItemMapper.toEntity(request);
        CatalogItem saved = catalogItemRepository.save(item);
        log.info("Created catalog item: id={} code={}", saved.getId(), saved.getItemCode());
        return catalogItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CatalogItemDto updateCatalogItem(Long id, UpdateCatalogItemRequest request) {
        CatalogItem item = findByIdOrThrow(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setActive(request.isActive());
        item.setStockQuantity(request.getStockQuantity());
        return catalogItemMapper.toDto(catalogItemRepository.save(item));
    }

    private CatalogItem findByIdOrThrow(Long id) {
        return catalogItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CatalogItem", "id", id));
    }
}
