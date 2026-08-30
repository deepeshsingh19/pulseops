package com.pulseops.api.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pulseops.api.kafka.config.KafkaTopics;
import com.pulseops.common.events.IncidentCreatedEvent;

@Component
public class IncidentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IncidentEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishIncidentCreated(
            IncidentCreatedEvent event) {

        /*
         * The incident key is used as the Kafka message key.
         * Messages with the same key are kept in partition order.
         */
        kafkaTemplate.send(
                KafkaTopics.INCIDENTS_CREATED,
                event.incidentKey(),
                event
        );
    }
}