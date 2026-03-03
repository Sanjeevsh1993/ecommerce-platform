package com.ecommerce.catalog.controller;

// BC References: BC-031, BC-032, BC-043

import com.ecommerce.shared.handler.GlobalExceptionHandler;
import com.ecommerce.catalog.config.SecurityConfig;
import com.ecommerce.catalog.dto.*;
import com.ecommerce.catalog.security.GatewayHeaderAuthFilter;
import com.ecommerce.catalog.service.CatalogService;
import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser(roles = "CUSTOMER")
class CatalogControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CatalogService catalogService;
    @MockBean GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @BeforeEach
    void setupFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(gatewayHeaderAuthFilter).doFilter(any(), any(), any());
    }

    private CatalogItemDto sampleItem() {
        return CatalogItemDto.builder()
                .id(1L).itemCode("ITEM-001").name("Widget")
                .price(new BigDecimal("9.99")).active(true).build();
    }

    // BC-031: GET /api/v1/catalog/items/{id}
    @Test
    void getCatalogItem_found_returns200() throws Exception {
        when(catalogService.getCatalogItem(1L)).thenReturn(sampleItem());

        mockMvc.perform(get("/api/v1/catalog/items/1")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCode").value("ITEM-001"))
                .andExpect(jsonPath("$.data.name").value("Widget"));
    }

    @Test
    void getCatalogItem_notFound_returns404() throws Exception {
        when(catalogService.getCatalogItem(99L))
                .thenThrow(new ResourceNotFoundException("CatalogItem", "id", 99L));

        mockMvc.perform(get("/api/v1/catalog/items/99")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    // BC-031: GET /api/v1/catalog/items/code/{code}
    @Test
    void getCatalogItemByCode_found_returns200() throws Exception {
        when(catalogService.getCatalogItemByCode("ITEM-001")).thenReturn(sampleItem());

        mockMvc.perform(get("/api/v1/catalog/items/code/ITEM-001")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCode").value("ITEM-001"));
    }

    // BC-031: GET /api/v1/catalog/items (list)
    @Test
    void listCatalogItems_returns200WithPage() throws Exception {
        var page = new PageImpl<>(List.of(sampleItem()), PageRequest.of(0, 20), 1);
        when(catalogService.listCatalogItems(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/catalog/items")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].itemCode").value("ITEM-001"));
    }

    // BC-032: POST /api/v1/catalog/items (admin only)
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCatalogItem_validRequest_returns201() throws Exception {
        CreateCatalogItemRequest req = new CreateCatalogItemRequest();
        req.setItemCode("NEW-001"); req.setName("New Widget");
        req.setPrice(new BigDecimal("5.00")); req.setActive(true);

        when(catalogService.createCatalogItem(any())).thenReturn(sampleItem());

        mockMvc.perform(post("/api/v1/catalog/items")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCatalogItem_duplicateCode_returns409() throws Exception {
        CreateCatalogItemRequest req = new CreateCatalogItemRequest();
        req.setItemCode("DUP-001"); req.setName("Dup"); req.setActive(true);

        when(catalogService.createCatalogItem(any()))
                .thenThrow(new DuplicateResourceException("CatalogItem", "itemCode", "DUP-001"));

        mockMvc.perform(post("/api/v1/catalog/items")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // BC-032: PUT /api/v1/catalog/items/{id}
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCatalogItem_validRequest_returns200() throws Exception {
        UpdateCatalogItemRequest req = new UpdateCatalogItemRequest();
        req.setName("Updated Widget"); req.setActive(true);
        req.setPrice(new BigDecimal("12.99"));

        when(catalogService.updateCatalogItem(eq(1L), any()))
                .thenReturn(CatalogItemDto.builder().id(1L).name("Updated Widget").build());

        mockMvc.perform(put("/api/v1/catalog/items/1")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Widget"));
    }
}
