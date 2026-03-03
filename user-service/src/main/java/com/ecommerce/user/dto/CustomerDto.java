package com.ecommerce.user.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CustomerDto {
    private Long id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String customerType;
    private boolean prospect;
    private String specialAssistanceIndicators;
    private String recommendationEngineUrl;
    private String crmUrl;
    private String createdAt;
    private String updatedAt;
}
