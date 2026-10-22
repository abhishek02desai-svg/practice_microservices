package com.example.orderservice.dto;

// Mirrors the JSON returned by USER-SERVICE (GET /api/users/{id})
public record UserResponse(Long id, String name, String email) {
}
