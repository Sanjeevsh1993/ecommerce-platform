package com.ecommerce.catalog.integration;

// STRANGLER FIG - Phase: 10 - Domain: Catalog Management — Integration Tests
// BC References: BC-031, BC-032, BC-053
// Uses TestContainers MySQL — same database engine as production

import com.ecommerce.catalog.dto.CreateCatalogItemRequest;
import com.ecommerce.catalog.dto.UpdateCatalogItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("it")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CatalogServiceIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ecommerce_catalog")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Shared state: track created item ID for subsequent tests
    private static Long createdItemId;

    // BC-032: create a catalog item with real DB persistence
    @Test
    @Order(1)
    void createCatalogItem_validRequest_returns201() throws Exception {
        CreateCatalogItemRequest req = buildCreateRequest("IT-ITEM-001", "Integration Test Widget",
                new BigDecimal("29.99"), "ELECTRONICS", 100);

        MvcResult result = mockMvc.perform(post("/api/v1/catalog/items")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itemCode").value("IT-ITEM-001"))
                .andExpect(jsonPath("$.data.name").value("Integration Test Widget"))
                .andExpect(jsonPath("$.data.price").value(29.99))
                .andExpect(jsonPath("$.data.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andReturn();

        // Capture ID for subsequent tests
        com.fasterxml.jackson.databind.JsonNode body =
                objectMapper.readTree(result.getResponse().getContentAsString());
        createdItemId = body.path("data").path("id").asLong();
    }

    // BC-031: retrieve catalog item by ID with real DB
    @Test
    @Order(2)
    void getCatalogItemById_existingItem_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/items/" + createdItemId)
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(createdItemId))
                .andExpect(jsonPath("$.data.itemCode").value("IT-ITEM-001"))
                .andExpect(jsonPath("$.data.stockQuantity").value(100));
    }

    // BC-031: retrieve catalog item by item code
    @Test
    @Order(3)
    void getCatalogItemByCode_existingCode_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/items/code/IT-ITEM-001")
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemCode").value("IT-ITEM-001"))
                .andExpect(jsonPath("$.data.name").value("Integration Test Widget"));
    }

    // BC-031: list catalog items returns persisted item
    @Test
    @Order(4)
    void listCatalogItems_returnsCreatedItem() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/items")
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // BC-031: search by query string returns matching item
    @Test
    @Order(5)
    void listCatalogItems_withQuery_filtersResults() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/items")
                        .param("query", "Widget")
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Integration Test Widget"));
    }

    // BC-032: duplicate itemCode is rejected with real DB unique constraint
    @Test
    @Order(6)
    void createCatalogItem_duplicateCode_returns409() throws Exception {
        CreateCatalogItemRequest req = buildCreateRequest("IT-ITEM-001", "Duplicate Item",
                new BigDecimal("9.99"), "OTHER", 5);

        mockMvc.perform(post("/api/v1/catalog/items")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    // BC-032: update catalog item — admin only
    @Test
    @Order(7)
    void updateCatalogItem_validRequest_returns200() throws Exception {
        UpdateCatalogItemRequest req = new UpdateCatalogItemRequest();
        req.setName("Updated Integration Widget");
        req.setPrice(new BigDecimal("34.99"));
        req.setStockQuantity(150);
        req.setActive(true);

        mockMvc.perform(put("/api/v1/catalog/items/" + createdItemId)
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Integration Widget"))
                .andExpect(jsonPath("$.data.price").value(34.99))
                .andExpect(jsonPath("$.data.stockQuantity").value(150));
    }

    // BC-053: non-admin cannot create catalog item
    @Test
    @Order(8)
    void createCatalogItem_asCustomer_returns403() throws Exception {
        CreateCatalogItemRequest req = buildCreateRequest("IT-ITEM-002", "Unauthorized Item",
                new BigDecimal("5.00"), "OTHER", 1);

        mockMvc.perform(post("/api/v1/catalog/items")
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // BC-031: non-existent ID returns 404
    @Test
    @Order(9)
    void getCatalogItemById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/items/99999")
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    private CreateCatalogItemRequest buildCreateRequest(String itemCode, String name,
                                                        BigDecimal price, String category,
                                                        int stockQuantity) {
        CreateCatalogItemRequest req = new CreateCatalogItemRequest();
        req.setItemCode(itemCode);
        req.setName(name);
        req.setPrice(price);
        req.setCategory(category);
        req.setActive(true);
        req.setStockQuantity(stockQuantity);
        return req;
    }
}
