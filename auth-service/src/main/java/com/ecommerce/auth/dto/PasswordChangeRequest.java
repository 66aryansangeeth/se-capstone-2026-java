package com.ecommerce.auth.dto;

public record PasswordChangeRequest(
    String oldPassword,
    String newPassword
) {}
