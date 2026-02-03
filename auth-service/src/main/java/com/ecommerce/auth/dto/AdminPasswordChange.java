package com.ecommerce.auth.dto;

public record AdminPasswordChange(
        String email,
        String NewPassword
) {}
