package com.pulseops.processor.kafka.producer;

import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.processor.kafka.config.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class IncidentDetectedProducer {

    private final KafkaTemplate<String, IncidentDetectedEvent> kafkaTemplate;

    public IncidentDetectedProducer(
            KafkaTemplate<String, IncidentDetectedEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(IncidentDetectedEvent event) {

        kafkaTemplate.send(
                KafkaTopics.INCIDENTS_DETECTED,
                event.incidentKey(),
                event
        );
    }
}