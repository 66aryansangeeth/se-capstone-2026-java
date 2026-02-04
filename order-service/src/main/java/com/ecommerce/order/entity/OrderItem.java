package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Integer quantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(precision = 10, scale = 2)
    private BigDecimal itemSubtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    public void calculateItemSubtotal() {
        if (priceAtPurchase != null && quantity != null) {
            this.itemSubtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }
}