package com.pulseops.processor.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic incidentsCreatedTopic() {

        return new NewTopic(
                KafkaTopics.INCIDENTS_CREATED,
                3,
                (short) 1
        );
    }

    @Bean
    public NewTopic incidentsDetectedTopic() {

        return new NewTopic(
                KafkaTopics.INCIDENTS_DETECTED,
                3,
                (short) 1
        );
    }

    @Bean
    public NewTopic telemetryEventsTopic() {

        return new NewTopic(
                KafkaTopics.TELEMETRY_EVENTS,
                6,
                (short) 1
        );
    }

    @Bean
    public NewTopic rcaRequestedTopic() {

        return new NewTopic(
                KafkaTopics.RCA_REQUESTED,
                3,
                (short) 1
        );
    }

    @Bean
    public NewTopic rcaCompletedTopic() {

        return new NewTopic(
                KafkaTopics.RCA_COMPLETED,
                3,
                (short) 1
        );
    }
}