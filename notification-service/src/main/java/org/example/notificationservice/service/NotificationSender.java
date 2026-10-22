package com.example.notificationservice.service;

import com.example.notificationservice.event.OrderPlacedEvent;

/**
 * How a notification is delivered. Today it only writes a log line.
 * Later, add another implementation (EmailNotificationSender, SmsNotificationSender, ...)
 * and the Kafka listener does not need to change.
 */
public interface NotificationSender {

    void sendOrderPlaced(OrderPlacedEvent event);
}
