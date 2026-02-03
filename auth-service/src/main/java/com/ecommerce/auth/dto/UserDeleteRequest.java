package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(
        @NotBlank(message = "email is required")
        @Email(message = "invalid email format")
        String email
) {}
