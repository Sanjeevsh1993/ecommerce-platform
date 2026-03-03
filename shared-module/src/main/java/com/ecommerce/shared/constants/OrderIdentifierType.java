package com.ecommerce.shared.constants;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: com.ecommerce.business.constants.OrderIdentifierType (static int constants → enum)
// BC References: BC-030 (order identifier type), BC-014 (post-save navigation rule)

/**
 * Order identifier type enumeration.
 *
 * Migrated from the old OrderIdentifierType class which used static int constants.
 * Converted to a proper Java enum preserving exact IDs.
 *
 * Business rule (BC-014, BC-015):
 * Post-save navigation only triggers when orderEntryTypeId == CUSTOMER(1).
 * If type is PRODUCT or ORDER, no navigation occurs (web service mode implied).
 */
public enum OrderIdentifierType {

    CUSTOMER(1),
    PRODUCT(2),
    ORDER(3);

    private final int id;

    OrderIdentifierType(int id) {
        this.id = id;
    }

    public int getId() { return id; }

    public static OrderIdentifierType fromId(int id) {
        for (OrderIdentifierType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Unknown OrderIdentifierType id: " + id);
    }
}
