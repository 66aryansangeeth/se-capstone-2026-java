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

        log.info("Log 1: Webhook endpoint hit with signature: {}", sigHeader != null ? sigHeader.substring(0, Math.min(20, sigHeader.length())) + "..." : "null");

        return Mono.fromCallable(() -> {
                    try {
                        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
                        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                        String eventType = event.getType();

                        log.info("Log 2: Stripe Event verified. Type: {}", eventType);

                        if ("checkout.session.completed".equals(eventType)) {
                            log.info("Log 3: Processing checkout.session.completed event");

                            Session session = dataObjectDeserializer.getObject()
                                    .map(stripeObject -> {
                                        log.info("Log 4: Successfully mapped Stripe object to Session");
                                        return (Session) stripeObject;
                                    })
                                    .orElseGet(() -> {
                                        log.warn("Log 4: Optional was empty, attempting deserializeUnsafe()");
                                        try {
                                            Session deserialized = (Session) dataObjectDeserializer.deserializeUnsafe();
                                            log.info("Log 4: Successfully deserialized Session using deserializeUnsafe()");
                                            return deserialized;
                                        } catch (Exception e) {
                                            log.error("Log 4: Critical error: Could not deserialize Stripe session: {}", e.getMessage(), e);
                                            return null;
                                        }
                                    });

                            if (session == null) {
                                log.error("Log 5: Session is NULL after deserialization attempts. Cannot process webhook.");
                                return Mono.empty();
                            }

                            String sessionId = session.getId();
                            String orderId = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
                            String paymentIntentId = session.getPaymentIntent();

                            log.info("Log 5: Session details - SessionId: {}, OrderId: {}, PaymentIntentId: {}", sessionId, orderId, paymentIntentId);

                            if (orderId == null || orderId.isEmpty()) {
                                log.error("Log 6: Order ID is NULL or EMPTY in session metadata. Cannot notify order service.");
                                return Mono.empty();
                            }

                            log.info("Log 6: Calling processSuccess for Order ID: {}", orderId);
                            return processSuccess(sessionId, orderId, paymentIntentId);
                        }

                        if ("payment_intent.payment_failed".equals(eventType)) {
                            log.info("Log 3: Processing payment_intent.payment_failed event");

                            com.stripe.model.PaymentIntent intent = dataObjectDeserializer.getObject()
                                    .map(stripeObject -> {
                                        log.info("Log 4: Successfully mapped Stripe object to PaymentIntent");
                                        return (com.stripe.model.PaymentIntent) stripeObject;
                                    })
                                    .orElseGet(() -> {
                                        log.warn("Log 4: Optional was empty, attempting deserializeUnsafe()");
                                        try {
                                            com.stripe.model.PaymentIntent deserialized = (com.stripe.model.PaymentIntent) dataObjectDeserializer.deserializeUnsafe();
                                            log.info("Log 4: Successfully deserialized PaymentIntent using deserializeUnsafe()");
                                            return deserialized;
                                        } catch (Exception e) {
                                            log.error("Log 4: Critical error: Could not deserialize Stripe payment intent: {}", e.getMessage(), e);
                                            return null;
                                        }
                                    });

                            if (intent == null) {
                                log.error("Log 5: PaymentIntent is NULL after deserialization attempts. Cannot process webhook.");
                                return Mono.empty();
                            }

                            String orderId = intent.getMetadata() != null ? intent.getMetadata().get("orderId") : null;
                            log.warn("Log 5: Payment FAILED received. OrderId: {}", orderId);

                            if (orderId == null || orderId.isEmpty()) {
                                log.error("Log 6: Order ID is NULL or EMPTY in payment intent metadata. Cannot notify order service.");
                                return Mono.empty();
                            }

                            log.info("Log 6: Calling notifyOrderService for Order ID: {} with action: cancel", orderId);
                            return notifyOrderService(orderId, "cancel");
                        }

                        log.info("Log 3: Event type '{}' is not handled by this webhook. Returning empty.", eventType);
                        return Mono.empty();
                    } catch (SignatureVerificationException e) {
                        log.error("Log 2: Webhook signature verification failed! Error: {}", e.getMessage());
                        return Mono.error(e);
                    } catch (Exception e) {
                        log.error("Log 2: Unexpected error processing webhook: {}", e.getMessage(), e);
                        return Mono.error(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(result -> {
                    log.info("Log 7: Executing Mono chain from handleWebhook");
                    return (Mono<?>) result;
                })
                .doOnSuccess(v -> log.info("Log 8: Webhook processing completed successfully"))
                .doOnError(e -> log.error("Log 8: Webhook processing failed with error: {}", e.getMessage(), e))
                .then();
    }

    private Mono<Void> processSuccess(String sessionId, String orderId, String paymentIntentId) {
        log.info("Log 9: processSuccess called - SessionId: {}, OrderId: {}, PaymentIntentId: {}", sessionId, orderId, paymentIntentId);
        return Mono.fromRunnable(() -> {
                    log.info("Log 10: Updating payment record in database for SessionId: {}", sessionId);
                    paymentRepository.findByStripeSessionId(sessionId).ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.SUCCEEDED);
                        payment.setPaymentIntentId(paymentIntentId);
                        paymentRepository.save(payment);
                        log.info("Log 10: Local payment record updated for Session: {}", sessionId);
                    });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> log.info("Log 11: Payment record update completed. Now calling notifyOrderService"))
                .then(notifyOrderService(orderId, "confirm"))
                .doOnError(e -> log.error("Log 11: Error in processSuccess: {}", e.getMessage(), e));
    }

    private Mono<Void> notifyOrderService(String orderId, String action) {
        if (orderServiceUrl == null || orderServiceUrl.isEmpty()) {
            log.error("Log 12: CRITICAL ERROR - Order Service URL is NULL or EMPTY! Cannot make PATCH request.");
            return Mono.error(new IllegalStateException("Order Service URL is not configured"));
        }

        if (orderId == null || orderId.isEmpty()) {
            log.error("Log 12: CRITICAL ERROR - Order ID is NULL or EMPTY! Cannot make PATCH request.");
            return Mono.error(new IllegalStateException("Order ID is null or empty"));
        }

        String finalUrl = orderServiceUrl + "/api/orders/" + orderId + "/" + action;
        log.info("Log 12: notifyOrderService called - Order ID: {}, Action: {}", orderId, action);
        log.info("Log 12: Order Service URL: {}", orderServiceUrl);
        log.info("Log 12: Full URL: {}", finalUrl);
        log.info("Log 12: Building WebClient and making PATCH request...");

        if (webClientBuilder == null) {
            log.error("Log 12: CRITICAL ERROR - WebClient.Builder is NULL!");
            return Mono.error(new IllegalStateException("WebClient.Builder is not available"));
        }

        return webClientBuilder.build().patch()
                .uri(orderServiceUrl + "/api/orders/{id}/" + action, orderId)
                .header("X-Internal-Secret", "my-app-secret-123")
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSubscribe(s -> log.info("Log 13: WebClient request SUBSCRIBED - PATCH request initiated"))
                .doOnSuccess(v -> log.info("Log 13: Successfully notified Order Service for Order ID: {} with action: {}", orderId, action))
                .doOnError(e -> log.error("Log 13: Failed to notify Order Service for Order ID: {} with action: {} - Error: {} | Error Type: {} | Full Stack Trace: ",
                        orderId, action, e.getMessage(), e.getClass().getName(), e))
                .onErrorResume(e -> {
                    log.error("Log 13: Error occurred, but continuing execution. Error: {}", e.getMessage());
                    return Mono.empty(); // Continue execution even if notification fails
                })
                .then()
                .doOnSuccess(v -> log.info("Log 14: notifyOrderService completed successfully"))
                .doOnError(e -> log.error("Log 14: notifyOrderService failed: {}", e.getMessage(), e));
    }
}