package com.pulseops.api.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration
public class KafkaProducerConfig {

    /*
     * Common producer settings shared by both producer factories.
     */
    private Map<String, Object> baseProducerProperties() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );

        properties.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        return properties;
    }

    /*
     * Used by the transactional outbox.
     *
     * OutboxPublisher already stores a JSON payload as String,
     * so this producer must continue using StringSerializer.
     */
    @Bean
    public ProducerFactory<String, String>
    stringProducerFactory() {

        Map<String, Object> properties =
                baseProducerProperties();

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                properties
        );
    }

    @Bean
    public KafkaTemplate<String, String>
    stringKafkaTemplate(
            ProducerFactory<String, String> producerFactory) {

        return new KafkaTemplate<>(
                producerFactory
        );
    }

    /*
     * Used for application domain events.
     *
     * These producers send Java event objects directly, so they
     * require JSON serialization.
     */
    @Bean
    public ProducerFactory<String, Object>
    jsonProducerFactory() {

        Map<String, Object> properties =
                baseProducerProperties();

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(
                properties
        );
    }

    @Bean
    public KafkaTemplate<String, Object>
    jsonKafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {

        return new KafkaTemplate<>(
                producerFactory
        );
    }
}