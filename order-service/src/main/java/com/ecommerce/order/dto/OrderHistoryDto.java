package com.ecommerce.order.dto;

// BC References: BC-013..025, BC-014 (navigationHint), BC-056 (webService mode)

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data @Builder
public class OrderHistoryDto {
    private Long id;
    private Long customerId;
    private String customerNumber;
    private Integer orderEntryTypeId;
    private String orderEntryTypeName;     // "New Order", "Order Update", etc.
    private Integer orderIdentifierTypeId;
    private String orderIdentifierTypeName;
    private String orderId;
    private LocalDate orderDate;
    private String notes;
    private boolean external;
    private Integer workQueueTypeId;
    private String workQueueTypeName;
    private Integer workQueueReasonId;
    private String workQueueReasonName;
    private String workQueueNotes;
    // BC-014/015: navigation hint returned after save
    private String navigationHint;    // "CUSTOMER_SUMMARY", "ORDER_HISTORY_LIST", "NONE"
    private String createdAt;
    private String updatedAt;
}
