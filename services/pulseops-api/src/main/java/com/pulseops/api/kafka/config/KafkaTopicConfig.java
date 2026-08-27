package com.pulseops.api.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public org.apache.kafka.clients.admin.NewTopic incidentsCreatedTopic() {

        /*
         * TopicBuilder is Spring Kafka's convenient API for defining
         * Kafka topics.
         *
         * We use:
         * - 3 partitions so consumers can process events in parallel later
         * - 1 replica because our local Kafka setup has only one broker
         */
        return TopicBuilder
                .name(KafkaTopics.INCIDENTS_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
    @Bean
    public NewTopic telemetryEventsTopic() {

        /*
        * Telemetry is expected to be much higher volume than incidents,
        * so we give it more partitions.
        *
        * Local environment:
        * 6 partitions
        * 1 replica
        */
        return TopicBuilder
                .name(KafkaTopics.TELEMETRY_EVENTS)
                .partitions(6)
                .replicas(1)
                .build();
    }
}