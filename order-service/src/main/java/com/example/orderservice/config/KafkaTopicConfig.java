package com.example.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Spring's KafkaAdmin creates this topic at startup if it does not exist yet.
    @Bean
    public NewTopic orderPlacedTopic(@Value("${app.kafka.topic.order-placed}") String name,
                                     @Value("${app.kafka.topic.partitions}") int partitions,
                                     @Value("${app.kafka.topic.replicas}") int replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
