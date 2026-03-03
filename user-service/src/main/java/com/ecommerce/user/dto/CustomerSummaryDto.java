package com.ecommerce.user.dto;

// BC References: BC-001 (customer summary), BC-005 (wishlist/B2B visibility), BC-006 (prospect), BC-009 (CRM URL)

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CustomerSummaryDto {
    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String customerType;        // short description: "B2C", "B2B", etc.
    private boolean prospect;
    // BC-005: computed visibility flags
    private boolean wishlistVisible;           // true only for B2C
    private boolean businessAccountVisible;    // true for B2B, RESELLER, WHOLESALE
    // BC-009: crmUrl only present for non-B2C, non-prospect
    private String crmUrl;
    // BC-008
    private String recommendationEngineUrl;
    // BC-007
    private String specialAssistanceIndicators;
    // BC-053: "Y" = test customer (UI shows red "TEST CUSTOMER" banner)
    private String testCustomerIndicator;
}
