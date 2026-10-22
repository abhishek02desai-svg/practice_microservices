package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDetailsResponse;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Rate limited: max 5 calls per 10 seconds (shared by the endpoints that call USER-SERVICE).
    // When the limit is exceeded, RequestNotPermitted is thrown and GlobalExceptionHandler returns 429.
    @RateLimiter(name = "orderApi")
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // Order + user details (WebClient + Mono)
    @RateLimiter(name = "orderApi")
    @GetMapping("/{id}/details")
    public ResponseEntity<OrderDetailsResponse> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    // All users from USER-SERVICE (WebClient + Flux)
    @RateLimiter(name = "orderApi")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(orderService.getAllUsers());
    }
}
