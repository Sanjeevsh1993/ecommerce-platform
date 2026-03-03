package com.ecommerce.order.service.impl;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistorySaveAddCommand, OrderHistoryAddCommand, OrderHistoryDelegate
// BC References: BC-013..025, BC-056

import com.ecommerce.order.client.UserServiceClient;
import com.ecommerce.order.constants.*;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.OrderHistory;
import com.ecommerce.order.repository.OrderHistoryRepository;
import com.ecommerce.order.service.OrderHistoryService;
import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.constants.OrderIdentifierType;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional(readOnly = true)
    public OrderHistoryListResponse listOrderHistory(Long customerId, boolean includeExternal) {
        // BC-013: pre-populate customer info via Feign
        var customerSummary = userServiceClient.getCustomerSummary(customerId).getData();
        // BC-020: filter external orders based on toggle flag
        List<OrderHistory> entries = orderHistoryRepository
                .findByCustomerIdWithExternalFilter(customerId, includeExternal);

        return OrderHistoryListResponse.builder()
                .customerId(customerId)
                .customerNumber(customerSummary != null ? customerSummary.getCustomerNumber() : "")
                .includeExternal(includeExternal)
                .entries(entries.stream().map(this::toDto).collect(Collectors.toList()))
                .totalCount(entries.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderHistoryDto getOrderHistory(Long orderHistoryId) {
        return toDto(findByIdOrThrow(orderHistoryId));
    }

    @Override
    @Transactional
    public OrderHistoryDto addOrderHistory(CreateOrderHistoryRequest request) {
        validateRequest(request);
        OrderHistory entity = buildEntity(request);
        OrderHistory saved = orderHistoryRepository.save(entity);
        log.info("OrderHistory added: id={} customerId={}", saved.getId(), saved.getCustomerId());
        OrderHistoryDto dto = toDto(saved);
        // BC-014/015/056: set navigation hint
        dto.setNavigationHint(resolveNavigationHint(request.isWebService(), request.isMaintainScreen()));
        return dto;
    }

    @Override
    @Transactional
    public OrderHistoryDto addOrderHistoryWithWorkQueue(CreateOrderHistoryRequest request) {
        // BC-016: validates work queue fields are present
        if (request.getWorkQueueTypeId() == null) {
            throw new BusinessRuleException("MISSING_WORK_QUEUE_TYPE", "Work queue type is required for work queue entries");
        }
        if (request.getWorkQueueReasonId() == null) {
            throw new BusinessRuleException("MISSING_WORK_QUEUE_REASON", "Work queue reason is required for work queue entries");
        }
        // BC-028: validate that reason ID is one of the known non-sequential IDs
        WorkQueueReason.fromId(request.getWorkQueueReasonId());
        return addOrderHistory(request);
    }

    @Override
    @Transactional
    public OrderHistoryDto updateOrderHistory(Long orderHistoryId, UpdateOrderHistoryRequest request) {
        OrderHistory entity = findByIdOrThrow(orderHistoryId);
        entity.setOrderEntryTypeId(request.getOrderEntryTypeId());
        entity.setOrderIdentifierTypeId(request.getOrderIdentifierTypeId());
        entity.setOrderId(request.getOrderId());
        entity.setOrderDate(request.getOrderDate());
        entity.setNotes(request.getNotes());
        entity.setExternal(request.isExternal());
        entity.setWorkQueueTypeId(request.getWorkQueueTypeId());
        entity.setWorkQueueReasonId(request.getWorkQueueReasonId());
        entity.setWorkQueueNotes(request.getWorkQueueNotes());
        OrderHistoryDto dto = toDto(orderHistoryRepository.save(entity));
        // BC-014/015/056: navigation hint on update too
        dto.setNavigationHint(resolveNavigationHint(request.isWebService(), request.isMaintainScreen()));
        return dto;
    }

    // BC-014/015/056: post-save navigation logic — mirrors OrderHistorySaveAddCommand
    private String resolveNavigationHint(boolean webService, boolean maintainScreen) {
        if (webService) {
            return AppConstants.REDIRECT_NONE;              // BC-056: web service mode — no navigation
        }
        return maintainScreen
                ? AppConstants.REDIRECT_ORDER_HISTORY_LIST  // BC-015: stay on maintain list
                : AppConstants.REDIRECT_CUSTOMER_SUMMARY;   // BC-014: return to customer summary
    }

    private OrderHistory buildEntity(CreateOrderHistoryRequest request) {
        return OrderHistory.builder()
                .customerId(request.getCustomerId())
                .customerNumber(request.getCustomerNumber())
                .orderEntryTypeId(request.getOrderEntryTypeId())
                .orderIdentifierTypeId(request.getOrderIdentifierTypeId())
                .orderId(request.getOrderId())
                .orderDate(request.getOrderDate())
                .notes(request.getNotes())
                .external(request.isExternal())
                .workQueueTypeId(request.getWorkQueueTypeId())
                .workQueueReasonId(request.getWorkQueueReasonId())
                .workQueueNotes(request.getWorkQueueNotes())
                .build();
    }

    private void validateRequest(CreateOrderHistoryRequest request) {
        // BC-025: validate order identifier type
        OrderIdentifierType.fromId(request.getOrderIdentifierTypeId());
        // BC-022: validate order entry type
        OrderEntryType.fromId(request.getOrderEntryTypeId());
    }

    private OrderHistory findByIdOrThrow(Long id) {
        return orderHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderHistory", "id", id));
    }

    private OrderHistoryDto toDto(OrderHistory e) {
        String orderTypeName = safeOrderTypeName(e.getOrderEntryTypeId());
        String identifierTypeName = safeIdentifierTypeName(e.getOrderIdentifierTypeId());
        String wqTypeName = e.getWorkQueueTypeId() != null ? safeWqTypeName(e.getWorkQueueTypeId()) : null;
        String wqReasonName = e.getWorkQueueReasonId() != null ? safeWqReasonName(e.getWorkQueueReasonId()) : null;

        return OrderHistoryDto.builder()
                .id(e.getId())
                .customerId(e.getCustomerId())
                .customerNumber(e.getCustomerNumber())
                .orderEntryTypeId(e.getOrderEntryTypeId())
                .orderEntryTypeName(orderTypeName)
                .orderIdentifierTypeId(e.getOrderIdentifierTypeId())
                .orderIdentifierTypeName(identifierTypeName)
                .orderId(e.getOrderId())
                .orderDate(e.getOrderDate())
                .notes(e.getNotes())
                .external(e.isExternal())
                .workQueueTypeId(e.getWorkQueueTypeId())
                .workQueueTypeName(wqTypeName)
                .workQueueReasonId(e.getWorkQueueReasonId())
                .workQueueReasonName(wqReasonName)
                .workQueueNotes(e.getWorkQueueNotes())
                .createdAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null)
                .updatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null)
                .build();
    }

    private String safeOrderTypeName(int id) {
        try { return OrderEntryType.fromId(id).getDisplayName(); } catch (Exception e) { return "Unknown"; }
    }
    private String safeIdentifierTypeName(int id) {
        try { return OrderIdentifierType.fromId(id).name(); } catch (Exception e) { return "Unknown"; }
    }
    private String safeWqTypeName(int id) {
        try { return WorkQueueType.fromId(id).getDisplayName(); } catch (Exception e) { return "Unknown"; }
    }
    private String safeWqReasonName(int id) {
        try { return WorkQueueReason.fromId(id).getDisplayName(); } catch (Exception e) { return "Unknown"; }
    }
}
