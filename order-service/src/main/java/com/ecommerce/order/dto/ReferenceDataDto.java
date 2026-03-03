package com.ecommerce.order.dto;

// BC References: BC-026..029

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class ReferenceDataDto {
    private int id;
    private String name;
    private String description;
}
