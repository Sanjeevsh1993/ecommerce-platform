package com.ecommerce.catalog.dto;

// BC References: BC-032

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateCatalogItemRequest {
    @NotBlank @Size(max = 255) private String name;
    @Size(max = 2000) private String description;
    @DecimalMin("0.00") private BigDecimal price;
    @Size(max = 100) private String category;
    private boolean active;
    @Min(0) private Integer stockQuantity;
}
