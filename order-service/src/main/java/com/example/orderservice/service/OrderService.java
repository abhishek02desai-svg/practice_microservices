package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDetailsResponse;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.entity.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Order order);

    List<Order> getAllOrders();

    Order getOrderById(Long id);

    OrderDetailsResponse getOrderDetails(Long id);

    List<UserResponse> getAllUsers();
}
