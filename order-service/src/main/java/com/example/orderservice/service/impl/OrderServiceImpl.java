package com.example.orderservice.service.impl;

import com.example.orderservice.dto.OrderDetailsResponse;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderEventPublisher;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.UserIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    // All calls to USER-SERVICE (OpenFeign / WebClient, plus the commented RestClient and
    // RestTemplate versions) are in UserIntegrationService, protected by Retry, Circuit Breaker and Fallback.
    private final UserIntegrationService userIntegrationService;

    // Publishes "order placed" events to Kafka (asynchronous, never throws)
    private final OrderEventPublisher orderEventPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserIntegrationService userIntegrationService,
                            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.userIntegrationService = userIntegrationService;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public Order createOrder(Order order) {
        if (order.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        userIntegrationService.getUserForOrder(order.getUserId()); // OpenFeign: 404 if user missing, 503 if service down
        order.setId(null);                                         // make sure a NEW row is always created
        Order savedOrder = orderRepository.save(order);      // committed to order_db when save() returns
        orderEventPublisher.publishOrderPlaced(savedOrder);  // only after a successful save; failures are logged, not thrown
        return savedOrder;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public OrderDetailsResponse getOrderDetails(Long id) {
        Order order = getOrderById(id);
        UserResponse user = userIntegrationService.getUserDetails(order.getUserId()); // WebClient (Mono)
        return new OrderDetailsResponse(order, user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userIntegrationService.getAllUsers(); // WebClient (Flux)
    }
}
