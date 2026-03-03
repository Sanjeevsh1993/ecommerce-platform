package com.ecommerce.shared.constants;

// STRANGLER FIG - Phase: 3 - Domain: Shared
// Migrated from: com.ecommerce.business.constants.CustomerType (factory pattern → Java enum)
// BC References: BC-005 (B2C/B2B conditional UI logic), BC-053 (customer data fields)

/**
 * Customer type enumeration.
 *
 * Migrated from the old CustomerType factory class which used integer constants
 * and a static instances map. Converted to a proper Java enum preserving
 * exact IDs, shortDescriptions, and longDescriptions.
 *
 * Business rule (BC-005):
 * - B2C → show Wishlist panel, hide Business Account panel
 * - B2B / RESELLER / WHOLESALE → show Business Account panel, hide Wishlist panel
 */
public enum CustomerType {

    B2C(1, "B2C", "Business to Consumer"),
    B2B(2, "B2B", "Business to Business"),
    RESELLER(3, "RESELLER", "Reseller"),
    WHOLESALE(4, "WHOLESALE", "Wholesale");

    private final int id;
    private final String shortDescription;
    private final String longDescription;

    CustomerType(int id, String shortDescription, String longDescription) {
        this.id = id;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    public int getId() { return id; }
    public String getShortDescription() { return shortDescription; }
    public String getLongDescription() { return longDescription; }

    /**
     * BC-005: Returns true if this customer type should show the Wishlist panel.
     * Only B2C customers see the Wishlist.
     */
    public boolean isWishlistVisible() {
        return this == B2C;
    }

    /**
     * BC-005: Returns true if this customer type should show the Business Account panel.
     * All non-B2C customers (B2B, RESELLER, WHOLESALE) see the Business Account.
     */
    public boolean isBusinessAccountVisible() {
        return this != B2C;
    }

    public static CustomerType fromId(int id) {
        for (CustomerType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Unknown CustomerType id: " + id);
    }

    public static CustomerType fromLongDescription(String longDescription) {
        for (CustomerType type : values()) {
            if (type.longDescription.equalsIgnoreCase(longDescription)) return type;
        }
        throw new IllegalArgumentException("Unknown CustomerType: " + longDescription);
    }
}
