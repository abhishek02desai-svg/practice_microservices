package com.example.orderservice.dto;

import com.example.orderservice.entity.Order;

// An order together with the user who placed it (user data comes from USER-SERVICE)
public record OrderDetailsResponse(Order order, UserResponse user) {
}
