package com.ecommerce.product.dto;



public record ProductRequest(
         String name,
         String description,
         Long price,
         Integer stockQuantity,
         String category
) { }
