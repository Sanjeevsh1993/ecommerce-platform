package com.ecommerce.catalog.service;

// BC References: BC-031, BC-032

import com.ecommerce.catalog.dto.*;
import com.ecommerce.catalog.entity.CatalogItem;
import com.ecommerce.catalog.mapper.CatalogItemMapper;
import com.ecommerce.catalog.repository.CatalogItemRepository;
import com.ecommerce.catalog.service.impl.CatalogServiceImpl;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock CatalogItemRepository catalogItemRepository;
    @Mock CatalogItemMapper catalogItemMapper;
    @InjectMocks CatalogServiceImpl service;

    private CatalogItem sampleItem() {
        CatalogItem item = CatalogItem.builder()
                .itemCode("ITEM-001").name("Test Widget")
                .price(new BigDecimal("9.99")).active(true).build();
        ReflectionTestUtils.setField(item, "id", 1L);
        return item;
    }

    // BC-031: get by ID
    @Test
    void getCatalogItem_found_returnsDto() {
        CatalogItem item = sampleItem();
        CatalogItemDto dto = CatalogItemDto.builder().id(1L).itemCode("ITEM-001").build();
        when(catalogItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(catalogItemMapper.toDto(item)).thenReturn(dto);

        CatalogItemDto result = service.getCatalogItem(1L);
        assertThat(result.getItemCode()).isEqualTo("ITEM-001");
    }

    @Test
    void getCatalogItem_notFound_throwsResourceNotFoundException() {
        when(catalogItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getCatalogItem(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // BC-031: get by code
    @Test
    void getCatalogItemByCode_found_returnsDto() {
        CatalogItem item = sampleItem();
        CatalogItemDto dto = CatalogItemDto.builder().itemCode("ITEM-001").build();
        when(catalogItemRepository.findByItemCode("ITEM-001")).thenReturn(Optional.of(item));
        when(catalogItemMapper.toDto(item)).thenReturn(dto);

        CatalogItemDto result = service.getCatalogItemByCode("ITEM-001");
        assertThat(result.getItemCode()).isEqualTo("ITEM-001");
    }

    // BC-032: create
    @Test
    void createCatalogItem_newCode_saves() {
        CreateCatalogItemRequest req = new CreateCatalogItemRequest();
        req.setItemCode("NEW-001"); req.setName("New Item");
        req.setPrice(new BigDecimal("5.00")); req.setActive(true);

        CatalogItem entity = sampleItem();
        CatalogItemDto dto = CatalogItemDto.builder().itemCode("NEW-001").build();

        when(catalogItemRepository.existsByItemCode("NEW-001")).thenReturn(false);
        when(catalogItemMapper.toEntity(req)).thenReturn(entity);
        when(catalogItemRepository.save(any())).thenReturn(entity);
        when(catalogItemMapper.toDto(entity)).thenReturn(dto);

        CatalogItemDto result = service.createCatalogItem(req);
        assertThat(result.getItemCode()).isEqualTo("NEW-001");
        verify(catalogItemRepository).save(any());
    }

    @Test
    void createCatalogItem_duplicateCode_throwsDuplicateResourceException() {
        CreateCatalogItemRequest req = new CreateCatalogItemRequest();
        req.setItemCode("DUP-001");
        when(catalogItemRepository.existsByItemCode("DUP-001")).thenReturn(true);

        assertThatThrownBy(() -> service.createCatalogItem(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // BC-032: update
    @Test
    void updateCatalogItem_exists_updatesFields() {
        CatalogItem item = sampleItem();
        UpdateCatalogItemRequest req = new UpdateCatalogItemRequest();
        req.setName("Updated Widget"); req.setActive(true);
        req.setPrice(new BigDecimal("12.99"));
        CatalogItemDto dto = CatalogItemDto.builder().name("Updated Widget").build();

        when(catalogItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(catalogItemRepository.save(any())).thenReturn(item);
        when(catalogItemMapper.toDto(any())).thenReturn(dto);

        CatalogItemDto result = service.updateCatalogItem(1L, req);
        assertThat(result.getName()).isEqualTo("Updated Widget");
    }

    // BC-031: list active items
    @Test
    void listCatalogItems_withQuery_returnsPage() {
        CatalogItem item = sampleItem();
        Page<CatalogItem> page = new PageImpl<>(List.of(item));
        CatalogItemDto dto = CatalogItemDto.builder().name("Test Widget").build();

        when(catalogItemRepository.searchActive(eq("Widget"), any(Pageable.class))).thenReturn(page);
        when(catalogItemMapper.toDto(item)).thenReturn(dto);

        Page<CatalogItemDto> result = service.listCatalogItems("Widget", Pageable.unpaged());
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Widget");
    }
}
