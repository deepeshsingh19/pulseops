package com.pulseops.api.kafka.config;

import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.common.events.IncidentRcaCompletedEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, IncidentDetectedEvent>
    incidentDetectedConsumerFactory() {

        Map<String, Object> properties =
                baseConsumerProperties();

        properties.put(
                "spring.json.value.default.type",
                "com.pulseops.common.events.IncidentDetectedEvent"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, IncidentDetectedEvent>
    incidentDetectedKafkaListenerContainerFactory(
            ConsumerFactory<String, IncidentDetectedEvent>
                    consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, IncidentDetectedEvent>();

        factory.setConsumerFactory(
                consumerFactory
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, IncidentRcaCompletedEvent>
    incidentRcaCompletedConsumerFactory() {

        Map<String, Object> properties =
                baseConsumerProperties();

        properties.put(
                "spring.json.value.default.type",
                "com.pulseops.common.events.IncidentRcaCompletedEvent"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, IncidentRcaCompletedEvent>
    incidentRcaCompletedKafkaListenerContainerFactory(
            ConsumerFactory<String, IncidentRcaCompletedEvent>
                    consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, IncidentRcaCompletedEvent>();

        factory.setConsumerFactory(
                consumerFactory
        );

        return factory;
    }

    private Map<String, Object> baseConsumerProperties() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        properties.put(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .KEY_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        properties.put(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .VALUE_DESERIALIZER_CLASS_CONFIG,
                ErrorHandlingDeserializer.class
        );

        properties.put(
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS,
                StringDeserializer.class
        );

        properties.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
                JacksonJsonDeserializer.class
        );

        /*
         * Only trust the shared event-contract package.
         */
        properties.put(
                "spring.json.trusted.packages",
                "com.pulseops.common.events"
        );

        return properties;
    }
}