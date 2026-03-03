package com.ecommerce.order.controller;

// BC References: BC-013..025, BC-014 (navigationHint), BC-016 (WQ endpoint), BC-043

import com.ecommerce.shared.handler.GlobalExceptionHandler;
import com.ecommerce.order.config.SecurityConfig;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.security.GatewayHeaderAuthFilter;
import com.ecommerce.order.service.OrderHistoryService;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.exception.BusinessRuleException;
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
import org.springframework.http.MediaType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderHistoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser(roles = "CUSTOMER")
class OrderHistoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderHistoryService orderHistoryService;
    @MockBean GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @BeforeEach
    void setupFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(gatewayHeaderAuthFilter).doFilter(any(), any(), any());
    }

    private OrderHistoryDto sampleDto(String navHint) {
        return OrderHistoryDto.builder()
                .id(1L).customerId(10L).customerNumber("CUST-001")
                .orderEntryTypeId(1).orderEntryTypeName("New Order")
                .orderIdentifierTypeId(1).orderId("ORD-999")
                .navigationHint(navHint)
                .build();
    }

    // BC-013/020: GET /api/v1/order-history/customer/{customerId}
    @Test
    void listOrderHistory_returns200WithList() throws Exception {
        OrderHistoryListResponse resp = OrderHistoryListResponse.builder()
                .customerId(10L).customerNumber("CUST-001")
                .includeExternal(false)
                .entries(List.of(sampleDto(null)))
                .totalCount(1)
                .build();
        when(orderHistoryService.listOrderHistory(10L, false)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/order-history/customer/10")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.includeExternal").value(false));
    }

    // BC-020: includeExternal=true toggle
    @Test
    void listOrderHistory_withIncludeExternalTrue_passesToService() throws Exception {
        OrderHistoryListResponse resp = OrderHistoryListResponse.builder()
                .customerId(10L).includeExternal(true).entries(List.of()).totalCount(0).build();
        when(orderHistoryService.listOrderHistory(10L, true)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/order-history/customer/10?includeExternal=true")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.includeExternal").value(true));
    }

    // BC-014: POST /api/v1/order-history — navigationHint=CUSTOMER_SUMMARY
    @Test
    void addOrderHistory_maintainScreenFalse_returnsCustomerSummaryHint() throws Exception {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(10L); req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1); req.setOrderIdentifierTypeId(1);
        req.setOrderId("ORD-100"); req.setMaintainScreen(false);

        when(orderHistoryService.addOrderHistory(any()))
                .thenReturn(sampleDto(AppConstants.REDIRECT_CUSTOMER_SUMMARY));

        mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.navigationHint").value(AppConstants.REDIRECT_CUSTOMER_SUMMARY));
    }

    // BC-015: maintainScreen=true → ORDER_HISTORY_LIST hint
    @Test
    void addOrderHistory_maintainScreenTrue_returnsOrderHistoryListHint() throws Exception {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(10L); req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1); req.setOrderIdentifierTypeId(1);
        req.setOrderId("ORD-101"); req.setMaintainScreen(true);

        when(orderHistoryService.addOrderHistory(any()))
                .thenReturn(sampleDto(AppConstants.REDIRECT_ORDER_HISTORY_LIST));

        mockMvc.perform(post("/api/v1/order-history")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.navigationHint").value(AppConstants.REDIRECT_ORDER_HISTORY_LIST));
    }

    // BC-016: POST /api/v1/order-history/work-queue — distinct WQ endpoint
    @Test
    void addOrderHistoryWithWorkQueue_missingType_returns422() throws Exception {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(10L); req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1); req.setOrderIdentifierTypeId(1);
        req.setOrderId("ORD-102");
        // workQueueTypeId intentionally missing

        when(orderHistoryService.addOrderHistoryWithWorkQueue(any()))
                .thenThrow(new BusinessRuleException("MISSING_WORK_QUEUE_TYPE", "Work queue type is required"));

        mockMvc.perform(post("/api/v1/order-history/work-queue")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("MISSING_WORK_QUEUE_TYPE"));
    }

    // BC-019: GET /api/v1/order-history/{id}
    @Test
    void getOrderHistory_found_returns200() throws Exception {
        when(orderHistoryService.getOrderHistory(1L)).thenReturn(sampleDto(null));

        mockMvc.perform(get("/api/v1/order-history/1")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("ORD-999"));
    }

    @Test
    void getOrderHistory_notFound_returns404() throws Exception {
        when(orderHistoryService.getOrderHistory(99L))
                .thenThrow(new ResourceNotFoundException("OrderHistory", "id", 99L));

        mockMvc.perform(get("/api/v1/order-history/99")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isNotFound());
    }
}
