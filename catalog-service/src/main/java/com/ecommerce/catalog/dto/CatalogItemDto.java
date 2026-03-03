package com.ecommerce.catalog.dto;

// BC References: BC-031, BC-032

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class CatalogItemDto {
    private Long id;
    private String itemCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean active;
    private Integer stockQuantity;
    private String createdAt;
    private String updatedAt;
}
