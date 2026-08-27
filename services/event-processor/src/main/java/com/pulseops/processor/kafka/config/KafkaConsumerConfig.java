package com.pulseops.processor.kafka.config;

import com.pulseops.common.events.IncidentCreatedEvent;
import com.pulseops.common.events.TelemetryEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    /*
     * Each Kafka topic carries a different event type.
     * Separate consumer factories keep deserialization explicit.
     */

    @Bean
    public ConsumerFactory<String, IncidentCreatedEvent>
    incidentCreatedConsumerFactory(KafkaProperties kafkaProperties) {

        var properties =
                kafkaProperties.buildConsumerProperties();

        var deserializer =
                new JacksonJsonDeserializer<>(
                        IncidentCreatedEvent.class
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, IncidentCreatedEvent>
    incidentCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, IncidentCreatedEvent> consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, IncidentCreatedEvent>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, TelemetryEvent>
    telemetryConsumerFactory(KafkaProperties kafkaProperties) {

        var properties =
                kafkaProperties.buildConsumerProperties();

        var deserializer =
                new JacksonJsonDeserializer<>(
                        TelemetryEvent.class
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new org.apache.kafka.common.serialization.StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, TelemetryEvent>
    telemetryKafkaListenerContainerFactory(
            ConsumerFactory<String, TelemetryEvent> consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, TelemetryEvent>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }
}