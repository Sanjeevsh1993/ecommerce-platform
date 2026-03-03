package com.ecommerce.order.dto;

// BC References: BC-013, BC-020 (includeExternal toggle)

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class OrderHistoryListResponse {
    private Long customerId;
    private String customerNumber;
    private boolean includeExternal;
    private List<OrderHistoryDto> entries;
    private int totalCount;
}
