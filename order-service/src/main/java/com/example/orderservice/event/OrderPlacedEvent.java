package com.example.orderservice.event;

import java.math.BigDecimal;

/**
 * Message sent to Kafka (topic "order-placed") when an order has been saved.
 * It is a separate DTO on purpose: other services must never depend on the Order entity.
 *
 * JSON example:
 * {"eventId":"...","eventType":"ORDER_PLACED","occurredAt":"2026-10-20T10:15:30.123Z",
 *  "orderId":1,"userId":1,"productName":"Laptop","amount":1299.99}
 *
 * occurredAt is an ISO-8601 UTC timestamp (always the same format).
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
    public static final String TYPE = "ORDER_PLACED";
}
