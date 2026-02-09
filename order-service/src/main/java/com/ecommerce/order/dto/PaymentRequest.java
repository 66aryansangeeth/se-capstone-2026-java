package com.ecommerce.order.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        Long amount,
        String customerEmail,
        String productName
) {}