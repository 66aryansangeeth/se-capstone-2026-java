package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Optional<Payment> findByOrder_Id(Long orderId);

}
