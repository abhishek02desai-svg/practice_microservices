package com.example.orderservice.event;

import com.example.orderservice.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes order events to Kafka.
 *
 * Rules:
 *  - call it ONLY after the order has been saved;
 *  - it never throws: a Kafka problem must not turn a successfully saved order into an error;
 *  - send() is asynchronous, the HTTP request does not wait for notification-service.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String topic;

    public OrderEventPublisher(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
                               @Value("${app.kafka.topic.order-placed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishOrderPlaced(Order order) {
        try {
            OrderPlacedEvent event = new OrderPlacedEvent(
                    UUID.randomUUID().toString(),
                    OrderPlacedEvent.TYPE,
                    Instant.now().toString(),          // ISO-8601, UTC
                    order.getId(),
                    order.getUserId(),
                    order.getProductName(),
                    order.getAmount());

            // The key (orderId) keeps all events of the same order in the same partition, in order.
            kafkaTemplate.send(topic, String.valueOf(order.getId()), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Could NOT publish OrderPlacedEvent {} for order {} to topic '{}'. "
                                            + "The order is saved, but no notification will be sent. Cause: {}",
                                    event.eventId(), order.getId(), topic, ex.toString());
                        } else {
                            log.info("Published OrderPlacedEvent {} for order {} to topic '{}' (partition {}, offset {})",
                                    event.eventId(), order.getId(), topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception ex) {
            log.error("Could NOT publish OrderPlacedEvent for order {}. The order is saved. Cause: {}",
                    order.getId(), ex.toString());
        }
    }
}
