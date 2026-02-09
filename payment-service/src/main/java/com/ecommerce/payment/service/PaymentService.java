package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${application.order-service.url}")
    private String orderUrl;

    public Mono<String> createCheckoutSession(PaymentRequest request) {
        log.info("DEBUG: Incoming PaymentRequest -> Email: [{}]",
                request.customerEmail());
        return Mono.fromCallable(() -> {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setCustomerEmail(request.customerEmail())
                    .setSuccessUrl(orderUrl +"/api/orders/my-orders")
                    .setCancelUrl(orderUrl +"/api/orders/my-orders")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(request.amount())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Order #" + request.orderId()).build())
                                    .build())
                            .build())
                    .putMetadata("orderId", request.orderId().toString())
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("orderId", request.orderId().toString())
                                    .build()
                    )
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
