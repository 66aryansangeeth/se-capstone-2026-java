package com.ecommerce.order.service;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.http.HttpHeaders;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient productWebClient;
    private final WebClient stripeWebClient;
    private final WebClient.Builder webClientBuilder;
    @Value("${application.payment-service.url}")
    private String paymentServiceUrl;

    public Mono<OrderResponse> placeOrder(OrderRequest request, String userEmail, String token) {
        return Flux.fromIterable(request.items())
                .flatMap(itemReq -> productWebClient.get()
                        .uri("/api/products/{id}", itemReq.productId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ProductResponse.class)
                        .map(prod -> {
                            if (prod.stockQuantity() < itemReq.quantity()) {
                                throw new RuntimeException("Insufficient stock for: " + prod.name());
                            }
                            return new ValidatedItem(itemReq, prod);
                        })
                )
                .collectList()
                .flatMap(validatedItems -> {
                    return saveFullOrder(validatedItems, userEmail, "PENDING", OrderStatus.PENDING)
                            .flatMap(savedOrder -> {

                                PaymentRequest paymentReq = new PaymentRequest(
                                        savedOrder.getId(),
                                        savedOrder.getTotalAmount(),
                                        userEmail,
                                        "Order #" + savedOrder.getId()
                                );

                                return webClientBuilder.build().post()
                                        .uri(paymentServiceUrl + "/api/payments/create-session")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                        .bodyValue(paymentReq)
                                        .retrieve()
                                        .bodyToMono(String.class)
                                        .map(stripeUrl -> mapToResponseWithUrl(savedOrder, stripeUrl));
                            });
                });
    }

    @Transactional
    public Mono<Void> confirmOrder(Long id) {
        return Mono.fromCallable(() -> {
                    Order order = orderRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

                    if(order.getStatus() == OrderStatus.CONFIRMED) {
                        return order;
                    }

                    order.setStatus(OrderStatus.CONFIRMED);
                    return orderRepository.save(order);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(savedOrder -> {
                    List<OrderItemRequest> items = savedOrder.getItems().stream()
                            .map(i -> new OrderItemRequest(i.getProductId(), i.getQuantity()))
                            .toList();
                    return reduceAllStock(items);
                })
                .then();
    }
    @Transactional
    protected Mono<Order> saveFullOrder(List<ValidatedItem> validatedItems, String userEmail, String payStatus, OrderStatus orderStatus) {
        return Mono.fromCallable(() -> {
            Order order = Order.builder()
                    .userEmail(userEmail)
                    .status(orderStatus)
                    .items(new ArrayList<>())
                    .build();

            for (ValidatedItem v : validatedItems) {
                OrderItem item = OrderItem.builder()
                        .productId(v.req().productId())
                        .quantity(v.req().quantity())
                        .priceAtPurchase(v.res().price())
                        .build();
                order.addOrderItem(item);
            }

            Order savedOrder = orderRepository.save(order);

            return savedOrder;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> reduceAllStock(List<OrderItemRequest> items) {
        return Flux.fromIterable(items)
                .flatMap(item -> productWebClient.patch()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/products/{id}/reduce-stock")
                                .queryParam("quantity", item.quantity())
                                .build(item.productId()))
                        .retrieve()
                        .bodyToMono(Void.class))
                .then();
    }
    public Flux<OrderResponse> getOrdersByUser(String email) {
        return Mono.fromCallable(() -> orderRepository.findByUserEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::mapToResponse);
    }

    public Mono<Order> getOrderById(Long id) {
        return Mono.fromCallable(() -> orderRepository.findByIdWithItems(id)
                        .orElseThrow(() -> new RuntimeException("Order not found with id: " + id)))
                .subscribeOn(Schedulers.boundedElastic());
    }


    private record ValidatedItem(OrderItemRequest req, ProductResponse res) {}
    public OrderResponse mapToResponse(Order order) {
      return mapToResponseWithUrl(order, null);
    }

    private OrderResponse mapToResponseWithUrl(Order order, String url) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getItemSubtotal()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getUserEmail(),
                itemResponses,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getOrderDate(),
                url
        );
    }
}