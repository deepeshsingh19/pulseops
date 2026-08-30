package com.pulseops.processor.kafka.config;

import com.pulseops.common.events.IncidentCreatedEvent;
import com.pulseops.common.events.RcaRequestedEvent;
import com.pulseops.common.events.TelemetryEvent;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    /*
     * KafkaProperties provides the connection and consumer settings,
     * while each Jackson deserializer is configured explicitly for the
     * event type handled by its listener.
     */
    private Map<String, Object> consumerProperties(
            KafkaProperties kafkaProperties) {

        Map<String, Object> properties =
                kafkaProperties.buildConsumerProperties();

        /*
         * The deserializers are supplied directly to the consumer factory,
         * so remove the corresponding configuration properties. This keeps
         * Jackson from being configured twice during consumer creation.
         */
        properties.remove(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .KEY_DESERIALIZER_CLASS_CONFIG
        );

        properties.remove(
                org.apache.kafka.clients.consumer.ConsumerConfig
                        .VALUE_DESERIALIZER_CLASS_CONFIG
        );

        properties.remove(
                "spring.json.trusted.packages"
        );

        properties.remove(
                "spring.json.value.default.type"
        );

        properties.remove(
                "spring.json.value.type.mapping"
        );

        return properties;
    }

    @Bean
    public ConsumerFactory<String, IncidentCreatedEvent>
    incidentCreatedConsumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> properties =
                consumerProperties(kafkaProperties);

        JacksonJsonDeserializer<IncidentCreatedEvent>
                jsonDeserializer =
                new JacksonJsonDeserializer<>(
                        IncidentCreatedEvent.class
                );

        jsonDeserializer.addTrustedPackages(
                "com.pulseops.common.events"
        );

        ErrorHandlingDeserializer<IncidentCreatedEvent>
                valueDeserializer =
                new ErrorHandlingDeserializer<>(
                        jsonDeserializer
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, IncidentCreatedEvent>
    incidentCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, IncidentCreatedEvent>
                    consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, IncidentCreatedEvent>();

        factory.setConsumerFactory(
                consumerFactory
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, TelemetryEvent>
    telemetryConsumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> properties =
                consumerProperties(kafkaProperties);

        JacksonJsonDeserializer<TelemetryEvent>
                jsonDeserializer =
                new JacksonJsonDeserializer<>(
                        TelemetryEvent.class
                );

        jsonDeserializer.addTrustedPackages(
                "com.pulseops.common.events"
        );

        ErrorHandlingDeserializer<TelemetryEvent>
                valueDeserializer =
                new ErrorHandlingDeserializer<>(
                        jsonDeserializer
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, TelemetryEvent>
    telemetryKafkaListenerContainerFactory(
            ConsumerFactory<String, TelemetryEvent>
                    consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, TelemetryEvent>();

        factory.setConsumerFactory(
                consumerFactory
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, RcaRequestedEvent>
    rcaRequestedConsumerFactory(
            KafkaProperties kafkaProperties) {

        Map<String, Object> properties =
                consumerProperties(kafkaProperties);

        JacksonJsonDeserializer<RcaRequestedEvent>
                jsonDeserializer =
                new JacksonJsonDeserializer<>(
                        RcaRequestedEvent.class
                );

        jsonDeserializer.addTrustedPackages(
                "com.pulseops.common.events"
        );

        ErrorHandlingDeserializer<RcaRequestedEvent>
                valueDeserializer =
                new ErrorHandlingDeserializer<>(
                        jsonDeserializer
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String, RcaRequestedEvent>
    rcaRequestedKafkaListenerContainerFactory(
            ConsumerFactory<String, RcaRequestedEvent>
                    consumerFactory) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<
                        String, RcaRequestedEvent>();

        factory.setConsumerFactory(
                consumerFactory
        );

        return factory;
    }
}