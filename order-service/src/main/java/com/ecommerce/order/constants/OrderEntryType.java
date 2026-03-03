package com.ecommerce.order.constants;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistoryTag.java hardcoded order types (1-5)
// BC References: BC-022, BC-029

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderEntryType {
    NEW_ORDER(1, "New Order"),
    ORDER_UPDATE(2, "Order Update"),
    ORDER_CANCELLATION(3, "Order Cancellation"),
    RETURN_REFUND(4, "Return/Refund"),
    SHIPPING_INQUIRY(5, "Shipping Inquiry");

    private final int id;
    private final String displayName;

    public static OrderEntryType fromId(int id) {
        for (OrderEntryType t : values()) {
            if (t.id == id) return t;
        }
        throw new IllegalArgumentException("Unknown OrderEntryType id: " + id);
    }
}
