package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        Long price,
        Integer stockQuantity
) {
}
