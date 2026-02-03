package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> placeOrder(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String token = jwt.getTokenValue();
        String userEmail = jwt.getSubject();

        return orderService.placeOrder(request, userEmail, token);
    }

    @GetMapping("/my-orders")
    public Flux<OrderResponse> getOrderHistory(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrdersByUser(jwt.getSubject());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // This blocks non-admins
    public Mono<OrderResponse> getOrderById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        System.out.println("User Claims: " + jwt.getClaims());
        return orderService.getOrderById(id)
                .map(orderService::mapToResponse);
    }
    @PatchMapping("/{id}/confirm")
    public Mono<Void> confirmOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {

        if (!"my-app-secret-123".equals(secret)) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        }
        return orderService.confirmOrder(id);
    }
}
