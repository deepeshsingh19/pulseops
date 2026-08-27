package com.pulseops.api.kafka.producer;

import com.pulseops.api.kafka.config.KafkaTopics;
import com.pulseops.common.events.IncidentCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class IncidentEventProducer {

    private final KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate;

    public IncidentEventProducer(
            KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishIncidentCreated(IncidentCreatedEvent event) {

        /*
         * We use incidentKey as the Kafka key.
         *
         * Kafka guarantees ordering for messages with the same key
         * within the same partition.
         */
        kafkaTemplate.send(
                KafkaTopics.INCIDENTS_CREATED,
                event.incidentKey(),
                event
        );
    }
}