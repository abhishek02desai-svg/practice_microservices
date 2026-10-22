package com.example.notificationservice.event;

import java.math.BigDecimal;

/**
 * The JSON message published by order-service on topic "order-placed".
 * This is notification-service's OWN copy of the event: the two services share the JSON format,
 * not Java classes, so they can be built and deployed independently.
 * Unknown extra JSON fields are ignored, so order-service can add fields later without breaking this service.
 */
public record OrderPlacedEvent(
        String eventId,
        String eventType,
        String occurredAt,
        Long orderId,
        Long userId,
        String productName,
        BigDecimal amount
) {
}
