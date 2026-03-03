package com.ecommerce.user.dto;

// BC References: BC-003

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCustomerRequest {
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @NotBlank @Email @Size(max = 255) private String email;
    @Size(max = 30) private String phone;
    @NotBlank private String customerType;   // "B2C", "B2B", "RESELLER", "WHOLESALE"
    private boolean prospect;
    @Size(max = 500) private String specialAssistanceIndicators;
    @Size(max = 500) private String recommendationEngineUrl;
    @Size(max = 500) private String crmUrl;
}
