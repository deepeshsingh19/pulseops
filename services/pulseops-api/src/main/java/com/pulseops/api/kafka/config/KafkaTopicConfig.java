package com.pulseops.api.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic incidentsCreatedTopic() {

        return TopicBuilder
                .name(KafkaTopics.INCIDENTS_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic telemetryEventsTopic() {

        return TopicBuilder
                .name(KafkaTopics.TELEMETRY_EVENTS)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic incidentsDetectedTopic() {

        return TopicBuilder
                .name(KafkaTopics.INCIDENTS_DETECTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rcaRequestedTopic() {

        return TopicBuilder
                .name(KafkaTopics.RCA_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rcaCompletedTopic() {

        return TopicBuilder
                .name(KafkaTopics.RCA_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}