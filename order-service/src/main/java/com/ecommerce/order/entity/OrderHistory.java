package com.ecommerce.order.entity;

// STRANGLER FIG - Phase: 6 - Domain: Order Management
// Migrated from: OrderHistoryVO.java, ViewOrderHistoryVO.java
// BC References: BC-013..025, BC-053

import com.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "order_history", schema = "ecommerce_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BC-013: link to customer
    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 50)
    private String customerNumber;

    // BC-022: order entry type ID (1-5)
    @Column(nullable = false)
    private Integer orderEntryTypeId;

    // BC-025: order identifier type (CUSTOMER=1, PRODUCT=2, ORDER=3)
    @Column(nullable = false)
    private Integer orderIdentifierTypeId;

    // BC-025: the actual order identifier value
    @Column(nullable = false, length = 100)
    private String orderId;

    @Column
    private LocalDate orderDate;

    @Column(length = 2000)
    private String notes;

    // BC-020: external orders flag
    @Column(nullable = false)
    @Builder.Default
    private boolean external = false;

    // BC-016/017: work queue fields — nullable (only set on WQ add)
    @Column
    private Integer workQueueTypeId;

    @Column
    private Integer workQueueReasonId;

    @Column(length = 2000)
    private String workQueueNotes;
}
