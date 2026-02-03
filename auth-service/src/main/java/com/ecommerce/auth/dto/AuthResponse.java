package com.ecommerce.auth.dto;


import lombok.Builder;

@Builder
public record AuthResponse (
    String token,
    String email,
    String role
) { }
