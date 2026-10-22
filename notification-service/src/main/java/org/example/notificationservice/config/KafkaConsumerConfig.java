package com.example.notificationservice.config;

import com.example.notificationservice.event.OrderPlacedEvent;
import com.example.notificationservice.exception.InvalidEventException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    // A small producer used ONLY to copy failed messages into the dead-letter topic.
    // It can write both raw bytes (messages that could not be parsed) and OrderPlacedEvent objects.
    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        Map<Class<?>, Serializer<?>> delegates = new HashMap<>();
        delegates.put(byte[].class, new ByteArraySerializer());
        delegates.put(OrderPlacedEvent.class, new JsonSerializer<OrderPlacedEvent>());

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(
                props, new StringSerializer(), new DelegatingByTypeSerializer(delegates));
        return new KafkaTemplate<>(factory);
    }

    // Spring Boot automatically uses this error handler for @KafkaListener methods.
    //  - processing fails      -> retry 3 more times, 1 second apart, then send to the dead-letter topic
    //  - message is malformed  -> no retries (they cannot help), sent straight to the dead-letter topic
    // After the message is sent to the dead-letter topic, its offset is committed and the consumer moves on.
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> dltKafkaTemplate,
                                                 @Value("${app.kafka.topic.order-placed-dlt}") String dltTopic) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, ex) -> new TopicPartition(dltTopic, record.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        handler.addNotRetryableExceptions(InvalidEventException.class);
        return handler;
    }

    // Dead-letter topic (same number of partitions as the main topic).
    @Bean
    public NewTopic orderPlacedDltTopic(@Value("${app.kafka.topic.order-placed-dlt}") String name,
                                        @Value("${app.kafka.topic.partitions}") int partitions,
                                        @Value("${app.kafka.topic.replicas}") int replicas) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
