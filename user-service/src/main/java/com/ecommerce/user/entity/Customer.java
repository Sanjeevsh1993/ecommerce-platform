package com.ecommerce.user.entity;

// STRANGLER FIG - Phase: 6 - Domain: User / Customer Management
// Migrated from: CustomerSummaryVO.java, CustomerBO.java
// BC References: BC-001..012, BC-053 (audit)

import com.ecommerce.shared.constants.CustomerType;
import com.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers", schema = "ecommerce_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BC-001: customerNumber used in display header
    @Column(nullable = false, unique = true, length = 50)
    private String customerNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    // BC-005: drives wishlist / business account visibility
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    // BC-006: prospect customers: no CRM link, no send-email button
    @Column(nullable = false)
    @Builder.Default
    private boolean prospect = false;

    // BC-007: special assistance flags stored comma-separated e.g. "hearing,visual"
    @Column(length = 500)
    private String specialAssistanceIndicators;

    // BC-008: recommendation engine URL
    @Column(length = 500)
    private String recommendationEngineUrl;

    // BC-009: CRM URL — shown only for non-B2C, non-prospect
    @Column(length = 500)
    private String crmUrl;

    // BC-053: test customer indicator — "Y" triggers red "TEST CUSTOMER" banner in UI
    @Column(length = 1)
    private String testCustomerIndicator;
}
