package com.ecommerce.order.client;

// BC References: BC-013 (fields needed from user-service for order form pre-population)

import lombok.Data;

@Data
public class CustomerSummaryClientDto {
    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String customerType;
}
