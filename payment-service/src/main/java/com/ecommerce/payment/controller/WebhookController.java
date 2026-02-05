package com.ecommerce.payment.controller;

import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PaymentRepository paymentRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${application.order-service.url}")
    private String orderServiceUrl;

    @PostMapping
    public Mono<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Log 1: Webhook endpoint hit with signature: {}", sigHeader);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            String eventType = event.getType();
            log.info("Log 2: Stripe Event verified. Type: {}", eventType);

            if ("checkout.session.completed".equals(eventType)) {
                return dataObjectDeserializer.getObject()
                        .map(stripeObject -> {
                            Session session = (Session) stripeObject;
                            String orderId = session.getMetadata().get("orderId");
                            log.info("Payment SUCCESS received for Order ID: {}", orderId);
                            return processSuccess(session.getId(), orderId, session.getPaymentIntent());
                        }).orElse(Mono.empty());
            }

            if ("payment_intent.payment_failed".equals(eventType)) {
                return dataObjectDeserializer.getObject()
                        .map(stripeObject -> {
                            com.stripe.model.PaymentIntent intent = (com.stripe.model.PaymentIntent) stripeObject;
                            String orderId = intent.getMetadata().get("orderId");
                            log.warn("Payment FAILED received for Order ID: {}", orderId);
                            return notifyOrderService(orderId, "cancel");
                        }).orElse(Mono.empty());
            }
            log.info("Log 5: Event type {} is not handled by this webhook.", eventType);
            return Mono.empty();
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed!");
            return Mono.error(e);
        }
    }

    private Mono<Void> processSuccess(String sessionId, String orderId, String paymentIntentId) {
        return Mono.fromRunnable(() -> {
                    paymentRepository.findByStripeSessionId(sessionId).ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        payment.setPaymentIntentId(paymentIntentId);
                        paymentRepository.save(payment);
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(notifyOrderService(orderId, "confirm"));
    }


    private Mono<Void> notifyOrderService(String orderId, String action) {
        String finalUrl = orderServiceUrl + "/api/orders/" + orderId + "/" + action;
        log.info("Log 6: Preparing PATCH request. URL: {} | Secret: my-app-secret-123", finalUrl);
        return webClientBuilder.build().patch()
                .uri(finalUrl)
                .header("X-Internal-Secret", "my-app-secret-123")
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Failed to notify Order Service for {}: {}", action, e.getMessage()))
                .then();
    }
}