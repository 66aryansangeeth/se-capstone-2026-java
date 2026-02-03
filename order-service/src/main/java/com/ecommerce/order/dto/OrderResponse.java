package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String userEmail,
        List<OrderItemResponse> items, // The detailed list
        BigDecimal totalAmount,
        String status,
        LocalDateTime orderDate,
        String checkoutUrl
) {}