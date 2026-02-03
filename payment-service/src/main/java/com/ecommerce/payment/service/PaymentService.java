package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Mono<String> createCheckoutSession(PaymentRequest request) {
        log.info("DEBUG: Incoming PaymentRequest -> Email: [{}]",
                request.customerEmail());
        return Mono.fromCallable(() -> {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(request.customerEmail())
                    .setSuccessUrl("http://localhost:8083/api/orders/success")
                    .setCancelUrl("http://localhost:8083/api/orders/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(request.amount().multiply(new BigDecimal(100)).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Order #" + request.orderId()).build())
                                    .build())
                            .build())
                    .putMetadata("orderId", request.orderId().toString())
                    .build();

            Session session = Session.create(params);

            Payment payment = Payment.builder()
                    .orderId(request.orderId())
                    .customerEmail(request.customerEmail())
                    .stripeSessionId(session.getId())
                    .status(PaymentStatus.PENDING)
                    .amount(request.amount())
                    .build();
            paymentRepository.save(payment);

            return session.getUrl();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
