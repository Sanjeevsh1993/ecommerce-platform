package com.ecommerce.user.controller;

// BC References: BC-001..012, BC-043 (ApiResponse)

import com.ecommerce.shared.exception.DuplicateResourceException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.shared.handler.GlobalExceptionHandler;
import com.ecommerce.user.config.SecurityConfig;
import com.ecommerce.user.dto.*;
import com.ecommerce.user.security.GatewayHeaderAuthFilter;
import com.ecommerce.user.service.CustomerService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser(roles = "CUSTOMER")
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CustomerService customerService;
    @MockBean GatewayHeaderAuthFilter gatewayHeaderAuthFilter;

    @BeforeEach
    void setupFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(gatewayHeaderAuthFilter).doFilter(any(), any(), any());
    }

    private CustomerSummaryDto sampleSummary() {
        return CustomerSummaryDto.builder()
                .id(1L).customerNumber("CUST-001")
                .firstName("Jane").lastName("Doe")
                .customerType("B2C")
                .wishlistVisible(true)
                .businessAccountVisible(false)
                .build();
    }

    // BC-001: GET /api/v1/customers/{id}/summary
    @Test
    void getCustomerSummary_found_returns200WithSummary() throws Exception {
        when(customerService.getCustomerSummary(1L)).thenReturn(sampleSummary());

        mockMvc.perform(get("/api/v1/customers/1/summary")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerNumber").value("CUST-001"))
                // BC-005: B2C → wishlist visible
                .andExpect(jsonPath("$.data.wishlistVisible").value(true))
                .andExpect(jsonPath("$.data.businessAccountVisible").value(false));
    }

    @Test
    void getCustomerSummary_notFound_returns404() throws Exception {
        when(customerService.getCustomerSummary(99L))
                .thenThrow(new ResourceNotFoundException("Customer", "id", 99L));

        mockMvc.perform(get("/api/v1/customers/99/summary")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    // BC-002: GET /api/v1/customers?query=Jane
    @Test
    void listCustomers_withQuery_returns200() throws Exception {
        var page = new PageImpl<>(List.of(
                CustomerDto.builder().id(1L).firstName("Jane").build()),
                PageRequest.of(0, 20), 1);
        when(customerService.listCustomers(eq("Jane"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers?query=Jane")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].firstName").value("Jane"));
    }

    // BC-003: POST /api/v1/customers
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCustomer_validRequest_returns201() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setFirstName("John"); req.setLastName("Smith");
        req.setEmail("john@example.com"); req.setCustomerType("B2B");

        CustomerDto created = CustomerDto.builder()
                .id(2L).email("john@example.com").customerType("B2B").build();
        when(customerService.createCustomer(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/customers")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCustomer_duplicateEmail_returns409() throws Exception {
        CreateCustomerRequest req = new CreateCustomerRequest();
        req.setFirstName("A"); req.setLastName("B");
        req.setEmail("dup@example.com"); req.setCustomerType("B2C");

        when(customerService.createCustomer(any()))
                .thenThrow(new DuplicateResourceException("Customer", "email", "dup@example.com"));

        mockMvc.perform(post("/api/v1/customers")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    // BC-004: PUT /api/v1/customers/{id}
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCustomer_validRequest_returns200() throws Exception {
        UpdateCustomerRequest req = new UpdateCustomerRequest();
        req.setFirstName("Updated"); req.setLastName("Name");
        req.setEmail("jane@example.com"); req.setCustomerType("B2C");

        CustomerDto updated = CustomerDto.builder()
                .id(1L).firstName("Updated").build();
        when(customerService.updateCustomer(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/customers/1")
                        .header("X-User-Email", "admin@example.com")
                        .header("X-User-Roles", "ROLE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"));
    }

    // BC-013: GET /api/v1/customers/by-number/{num}/summary
    @Test
    void getCustomerSummaryByNumber_found_returns200() throws Exception {
        when(customerService.getCustomerSummaryByNumber("CUST-001")).thenReturn(sampleSummary());

        mockMvc.perform(get("/api/v1/customers/by-number/CUST-001/summary")
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerNumber").value("CUST-001"));
    }
}
