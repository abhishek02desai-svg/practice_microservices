package com.example.notificationservice.listener;

import com.example.notificationservice.event.OrderPlacedEvent;
import com.example.notificationservice.exception.InvalidEventException;
import com.example.notificationservice.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedEventListener.class);

    private final NotificationSender notificationSender;

    public OrderPlacedEventListener(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    // If this method throws, the offset is NOT committed: the error handler retries, then sends the
    // message to the dead-letter topic (see KafkaConsumerConfig).
    @KafkaListener(topics = "${app.kafka.topic.order-placed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderPlaced(OrderPlacedEvent event,
                              @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                              @Header(KafkaHeaders.OFFSET) long offset) {

        if (event == null || event.orderId() == null) {
            throw new InvalidEventException("OrderPlacedEvent without orderId (partition " + partition
                    + ", offset " + offset + ")");
        }

        log.info("Received OrderPlacedEvent {} (order {}) from partition {}, offset {}",
                event.eventId(), event.orderId(), partition, offset);

        notificationSender.sendOrderPlaced(event);
    }
}
