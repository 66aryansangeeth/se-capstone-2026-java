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

        return Mono.fromCallable(() -> {
                    try {
                        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

                        if ("checkout.session.completed".equals(event.getType())) {
                            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

                            Session session = dataObjectDeserializer.getObject()
                                    .map(stripeObject -> (Session) stripeObject)
                                    .orElseGet(() -> {
                                        try {
                                            return (Session) dataObjectDeserializer.deserializeUnsafe();
                                        } catch (Exception e) {
                                            log.error("Critical error: Could not deserialize Stripe session: {}", e.getMessage());
                                            return null;
                                        }
                                    });

                            if (session != null) {
                                String sessionId = session.getId();
                                String orderId = session.getMetadata().get("orderId");
                                String paymentIntentId = session.getPaymentIntent();

                                log.info("Webhook received successfully for Order ID: {}", orderId);
                                return processSuccess(sessionId, orderId, paymentIntentId);
                            }
                        }
                        return Mono.empty();
                    } catch (SignatureVerificationException e) {
                        log.error("Webhook signature verification failed!");
                        return Mono.error(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> (Mono<?>) result)
                .then();
    }

    private Mono<Void> processSuccess(String sessionId, String orderId, String paymentIntentId) {
        return Mono.fromRunnable(() -> {
                    paymentRepository.findByStripeSessionId(sessionId).ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        payment.setPaymentIntentId(paymentIntentId);
                        paymentRepository.save(payment);
                        log.info("Local payment record updated for Session: {}", sessionId);
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(notifyOrderService(orderId));
    }

    private Mono<Void> notifyOrderService(String orderId) {
        return webClientBuilder.build().patch()
                .uri(orderServiceUrl + "/{id}/confirm", orderId)
                .header("X-Internal-Secret", "my-app-secret-123")
                .retrieve()
                .bodyToMono(Void.class)
                .then();
    }
}