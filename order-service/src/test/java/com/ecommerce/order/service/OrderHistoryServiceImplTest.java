package com.ecommerce.order.service;

// BC References: BC-013..025, BC-014 (navigation hint), BC-016 (WQ validation), BC-028 (non-sequential reason IDs)

import com.ecommerce.order.client.CustomerSummaryClientDto;
import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.OrderHistory;
import com.ecommerce.order.repository.OrderHistoryRepository;
import com.ecommerce.order.service.impl.OrderHistoryServiceImpl;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.shared.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceImplTest {

    @Mock OrderHistoryRepository orderHistoryRepository;
    @Mock UserServiceClient userServiceClient;
    @InjectMocks OrderHistoryServiceImpl service;

    private CreateOrderHistoryRequest validRequest() {
        CreateOrderHistoryRequest req = new CreateOrderHistoryRequest();
        req.setCustomerId(1L);
        req.setCustomerNumber("CUST-001");
        req.setOrderEntryTypeId(1);    // NEW_ORDER
        req.setOrderIdentifierTypeId(1); // CUSTOMER
        req.setOrderId("ORD-12345");
        return req;
    }

    private OrderHistory savedEntity(Long id) {
        OrderHistory h = OrderHistory.builder()
                .customerId(1L).customerNumber("CUST-001")
                .orderEntryTypeId(1).orderIdentifierTypeId(1)
                .orderId("ORD-12345").build();
        ReflectionTestUtils.setField(h, "id", id);
        return h;
    }

    // BC-014: maintainScreen=false → REDIRECT_CUSTOMER_SUMMARY
    @Test
    void addOrderHistory_maintainScreenFalse_returnsCustomerSummaryHint() {
        CreateOrderHistoryRequest req = validRequest();
        req.setMaintainScreen(false);
        when(orderHistoryRepository.save(any())).thenReturn(savedEntity(10L));

        OrderHistoryDto result = service.addOrderHistory(req);

        assertThat(result.getNavigationHint()).isEqualTo(AppConstants.REDIRECT_CUSTOMER_SUMMARY);
    }

    // BC-015: maintainScreen=true → REDIRECT_ORDER_HISTORY_LIST
    @Test
    void addOrderHistory_maintainScreenTrue_returnsOrderHistoryListHint() {
        CreateOrderHistoryRequest req = validRequest();
        req.setMaintainScreen(true);
        when(orderHistoryRepository.save(any())).thenReturn(savedEntity(11L));

        OrderHistoryDto result = service.addOrderHistory(req);

        assertThat(result.getNavigationHint()).isEqualTo(AppConstants.REDIRECT_ORDER_HISTORY_LIST);
    }

    // BC-056: webService=true → REDIRECT_NONE
    @Test
    void addOrderHistory_webServiceTrue_returnsNoneHint() {
        CreateOrderHistoryRequest req = validRequest();
        req.setWebService(true);
        when(orderHistoryRepository.save(any())).thenReturn(savedEntity(12L));

        OrderHistoryDto result = service.addOrderHistory(req);

        assertThat(result.getNavigationHint()).isEqualTo(AppConstants.REDIRECT_NONE);
    }

    // BC-016: work queue add requires workQueueTypeId
    @Test
    void addOrderHistoryWithWorkQueue_missingType_throwsBusinessRuleException() {
        CreateOrderHistoryRequest req = validRequest();
        req.setWorkQueueTypeId(null);  // missing

        assertThatThrownBy(() -> service.addOrderHistoryWithWorkQueue(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Work queue type");
    }

    // BC-028: reason IDs are non-sequential — 101 is valid
    @Test
    void addOrderHistoryWithWorkQueue_validNonSequentialReasonId_succeeds() {
        CreateOrderHistoryRequest req = validRequest();
        req.setWorkQueueTypeId(1);
        req.setWorkQueueReasonId(101);  // BC-028: non-sequential ID — should be valid
        when(orderHistoryRepository.save(any())).thenReturn(savedEntity(13L));

        assertThatCode(() -> service.addOrderHistoryWithWorkQueue(req)).doesNotThrowAnyException();
    }

    // BC-028: reason ID 100 does NOT exist — must throw
    @Test
    void addOrderHistoryWithWorkQueue_invalidReasonId_throwsException() {
        CreateOrderHistoryRequest req = validRequest();
        req.setWorkQueueTypeId(1);
        req.setWorkQueueReasonId(100);  // BC-028: 100 is not a valid reason ID

        assertThatThrownBy(() -> service.addOrderHistoryWithWorkQueue(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // BC-013/020: list with external filter
    @Test
    void listOrderHistory_includeExternal_false_callsFilteredQuery() {
        CustomerSummaryClientDto cust = new CustomerSummaryClientDto();
        cust.setCustomerNumber("CUST-001");
        when(userServiceClient.getCustomerSummary(1L)).thenReturn(ApiResponse.success(cust));
        when(orderHistoryRepository.findByCustomerIdWithExternalFilter(1L, false)).thenReturn(List.of());

        OrderHistoryListResponse result = service.listOrderHistory(1L, false);

        assertThat(result.isIncludeExternal()).isFalse();
        verify(orderHistoryRepository).findByCustomerIdWithExternalFilter(1L, false);
    }

    // BC-019: get single entry
    @Test
    void getOrderHistory_notFound_throwsResourceNotFoundException() {
        when(orderHistoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getOrderHistory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
