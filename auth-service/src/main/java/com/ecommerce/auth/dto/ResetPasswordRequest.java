package com.ecommerce.auth.dto;

// BC References: BC-037 (password reset with token + new password)

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be 8–128 characters")
    private String newPassword;
}
