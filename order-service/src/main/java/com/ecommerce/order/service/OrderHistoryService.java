package com.ecommerce.order.service;

// BC References: BC-013..025

import com.ecommerce.order.dto.*;

public interface OrderHistoryService {
    // BC-013/020: list with external toggle
    OrderHistoryListResponse listOrderHistory(Long customerId, boolean includeExternal);
    // BC-019: view single entry
    OrderHistoryDto getOrderHistory(Long orderHistoryId);
    // BC-014/015: regular add (navigation hint from maintainScreen)
    OrderHistoryDto addOrderHistory(CreateOrderHistoryRequest request);
    // BC-016: work queue add (same entity, different navigation context)
    OrderHistoryDto addOrderHistoryWithWorkQueue(CreateOrderHistoryRequest request);
    // BC-018: update
    OrderHistoryDto updateOrderHistory(Long orderHistoryId, UpdateOrderHistoryRequest request);
}
