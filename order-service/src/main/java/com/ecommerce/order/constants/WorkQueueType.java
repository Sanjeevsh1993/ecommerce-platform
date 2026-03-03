package com.ecommerce.order.constants;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistoryTag.java work queue type dropdown
// BC References: BC-023, BC-027

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkQueueType {
    MISSING_REQUIRED_INFO(1, "Missing Required Info"),
    ORDER_ISSUE(2, "Order Issue"),
    BILLING_ISSUE(3, "Billing Issue"),
    SHIPPING_ISSUE(4, "Shipping Issue"),
    CUSTOMER_SERVICE_ISSUE(5, "Customer Service Issue");

    private final int id;
    private final String displayName;

    public static WorkQueueType fromId(int id) {
        for (WorkQueueType t : values()) {
            if (t.id == id) return t;
        }
        throw new IllegalArgumentException("Unknown WorkQueueType id: " + id);
    }
}
