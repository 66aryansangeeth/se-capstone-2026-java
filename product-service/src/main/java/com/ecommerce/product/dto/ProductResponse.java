package com.ecommerce.product.dto;


public record ProductResponse (
        Long id,
        String name,
        String description,
        Long price,
        Integer stockQuantity,
        String category,
        boolean isAvailable
){
}
