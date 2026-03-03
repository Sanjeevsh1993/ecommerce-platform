package com.ecommerce.catalog.entity;

// STRANGLER FIG - Phase: 6 - Domain: Catalog Management
// Migrated from: CatalogManagementFlowCommandExecutor.java → maintainCatalogItem.jsp
// BC References: BC-031, BC-032, BC-053

import com.ecommerce.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "catalog_items", schema = "ecommerce_catalog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String itemCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column
    private Integer stockQuantity;
}
