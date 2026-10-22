package com.example.notificationservice.service.impl;

import com.example.notificationservice.event.OrderPlacedEvent;
import com.example.notificationservice.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// Simulated notification: just writes a message to the console.
@Service
public class LoggingNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationSender.class);

    @Override
    public void sendOrderPlaced(OrderPlacedEvent event) {
        log.info("Notification: Order {} has been placed successfully for user {}.",
                event.orderId(), event.userId());
    }
}
