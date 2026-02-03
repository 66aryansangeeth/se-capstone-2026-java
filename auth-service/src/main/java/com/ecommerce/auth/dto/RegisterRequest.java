package com.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "invalid format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min=6, message= "Password must be of minimum 6 characters.")
        String password,

        @NotBlank(message = "Role is required")
        String role


) { }
