package com.ecommerce.order.constants;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistoryTag.java work queue reason dropdown
// BC References: BC-024, BC-028
// CRITICAL: IDs are NON-SEQUENTIAL — 101-103 and 201-202. Must NOT be renumbered.

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkQueueReason {
    // BC-028: reason IDs start at 101 (not 1) — preserve exact values
    MISSING_PAYMENT_INFO(101, "Missing Payment Info"),
    MISSING_SHIPPING_INFO(102, "Missing Shipping Info"),
    MISSING_CONTACT_INFO(103, "Missing Contact Info"),
    ORDER_VERIFICATION_REQUIRED(201, "Order Verification Required"),
    ORDER_CANCELLATION_REQUEST(202, "Order Cancellation Request");

    private final int id;
    private final String displayName;

    public static WorkQueueReason fromId(int id) {
        for (WorkQueueReason r : values()) {
            if (r.id == id) return r;
        }
        throw new IllegalArgumentException("Unknown WorkQueueReason id: " + id);
    }
}
