package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private String userEmail;
//    private Long productId;
//    private Integer quantity;

//    @Column(precision = 10, scale = 2)
//    private BigDecimal unitPriceAtPurchase;
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<OrderItem> items = new ArrayList<>();


    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @PrePersist
    public void setupOrder() {
        if(this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        calculateTotal();
    }

    // 3. Update the calculation logic
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}



