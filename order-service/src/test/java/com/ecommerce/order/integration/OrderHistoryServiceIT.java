package com.ecommerce.order.integration;

// STRANGLER FIG - Phase: 10 - Domain: Order Management — Integration Tests
// BC References: BC-013..025, BC-014, BC-015, BC-016, BC-028, BC-056
// Uses TestContainers MySQL — same database engine as production

import com.ecommerce.order.client.CustomerSummaryClientDto;
import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("it")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderHistoryServiceIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("ecommerce_orders")
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

    // Feign client mocked — user-service not running in IT context
    @MockBean UserServiceClient userServiceClient;

    @BeforeEach
    void setupMocks() {
        CustomerSummaryClientDto cust = new CustomerSummaryClientDto();
        cust.setId(1L); cust.setCustomerNumber("CUST-001");
        cust.setFirstName("Test"); cust.setLastName("Customer");
        when(userServiceClient.getCustomerSummary(anyLong()))
                .thenReturn(ApiResponse.success(cust));
    }

    // BC-014: add order history + verify navigationHint=CUSTOMER_SUMMARY persisted
    @Test
    @Order(1)
    void addOrderHistory_maintainScreenFalse_returnsCustSummaryHint() throws Exception {
        CreateOrderHistoryRequest req = buildRequest("IT-ORD-001", false, false, null, null);

        mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.navigationHint").value(AppConstants.REDIRECT_CUSTOMER_SUMMARY))
                .andExpect(jsonPath("$.data.orderId").value("IT-ORD-001"));
    }

    // BC-015: maintainScreen=true → ORDER_HISTORY_LIST
    @Test
    @Order(2)
    void addOrderHistory_maintainScreenTrue_returnsOrderHistoryListHint() throws Exception {
        CreateOrderHistoryRequest req = buildRequest("IT-ORD-002", true, false, null, null);

        mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.navigationHint").value(AppConstants.REDIRECT_ORDER_HISTORY_LIST));
    }

    // BC-056: webService=true → NONE
    @Test
    @Order(3)
    void addOrderHistory_webServiceTrue_returnsNoneHint() throws Exception {
        CreateOrderHistoryRequest req = buildRequest("IT-ORD-003", false, true, null, null);

        mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.navigationHint").value(AppConstants.REDIRECT_NONE));
    }

    // BC-016/028: work queue add with non-sequential reason ID 101
    @Test
    @Order(4)
    void addWorkQueueOrderHistory_validReasonId101_succeeds() throws Exception {
        CreateOrderHistoryRequest req = buildRequest("IT-WQ-001", false, false, 1, 101);

        mockMvc.perform(post("/api/v1/order-history/work-queue")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.workQueueTypeId").value(1))
                .andExpect(jsonPath("$.data.workQueueReasonId").value(101))
                // BC-028: reason name must reflect ID 101
                .andExpect(jsonPath("$.data.workQueueReasonName").value("Missing Payment Info"));
    }

    // BC-028: non-sequential reason ID 201 is valid
    @Test
    @Order(5)
    void addWorkQueueOrderHistory_validReasonId201_succeeds() throws Exception {
        CreateOrderHistoryRequest req = buildRequest("IT-WQ-002", false, false, 2, 201);

        mockMvc.perform(post("/api/v1/order-history/work-queue")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.workQueueReasonId").value(201))
                .andExpect(jsonPath("$.data.workQueueReasonName").value("Order Verification Required"));
    }

    // BC-013/020: list order history with real DB records
    @Test
    @Order(6)
    void listOrderHistory_returnsEntries() throws Exception {
        mockMvc.perform(get("/api/v1/order-history/customer/1")
                        .header("X-User-Email", "agent@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(1))
                .andExpect(jsonPath("$.data.includeExternal").value(false));
    }

    // BC-029: reference data — order entry types (no auth needed, permitAll)
    @Test
    @Order(7)
    void getOrderEntryTypes_returns5Types() throws Exception {
        mockMvc.perform(get("/api/v1/reference-data/order-entry-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("New Order"));
    }

    // BC-028: work queue reasons have non-sequential IDs in real API response
    @Test
    @Order(8)
    void getWorkQueueReasons_hasNonSequentialIds() throws Exception {
        mockMvc.perform(get("/api/v1/reference-data/work-queue-reasons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(101))   // NOT 1
                .andExpect(jsonPath("$.data[3].id").value(201));  // NOT 4
    }

    private CreateOrderHistoryRequest buildRequest(String orderId, boolean maintainScreen,
                                                    boolean webService, Integer wqTypeId, Integer wqReasonId) {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(1L); req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1); req.setOrderIdentifierTypeId(1);
        req.setOrderId(orderId);
        req.setMaintainScreen(maintainScreen); req.setWebService(webService);
        req.setWorkQueueTypeId(wqTypeId); req.setWorkQueueReasonId(wqReasonId);
        return req;
    }
}
