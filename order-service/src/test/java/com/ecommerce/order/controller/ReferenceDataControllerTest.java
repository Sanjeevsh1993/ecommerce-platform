package com.ecommerce.order.controller;

// BC References: BC-026..029

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import com.ecommerce.order.config.SecurityConfig;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.ecommerce.order.security.GatewayHeaderAuthFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferenceDataController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class ReferenceDataControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @BeforeEach
    void setupFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(gatewayHeaderAuthFilter).doFilter(any(), any(), any());
    }

    // BC-029: order entry types 1-5
    @Test
    void getOrderEntryTypes_returns5Types() throws Exception {
        mockMvc.perform(get("/api/v1/reference-data/order-entry-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("New Order"));
    }

    // BC-027: work queue types 1-5
    @Test
    void getWorkQueueTypes_returns5Types() throws Exception {
        mockMvc.perform(get("/api/v1/reference-data/work-queue-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));
    }

    // BC-028: work queue reasons — non-sequential IDs (101-103, 201-202)
    @Test
    void getWorkQueueReasons_returns5ReasonsWithNonSequentialIds() throws Exception {
        mockMvc.perform(get("/api/v1/reference-data/work-queue-reasons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                // BC-028: first reason ID must be 101, NOT 1
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].name").value("Missing Payment Info"))
                // BC-028: fourth reason ID must be 201
                .andExpect(jsonPath("$.data[3].id").value(201))
                .andExpect(jsonPath("$.data[3].name").value("Order Verification Required"));
    }
}
