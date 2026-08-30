package com.pulseops.processor.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pulseops.common.events.IncidentRcaCompletedEvent;
import com.pulseops.processor.kafka.config.KafkaTopics;

@Component
public class IncidentRcaCompletedProducer {

    private final KafkaTemplate<String, IncidentRcaCompletedEvent>
            kafkaTemplate;

    public IncidentRcaCompletedProducer(
            KafkaTemplate<String, IncidentRcaCompletedEvent>
                    kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            IncidentRcaCompletedEvent event) {

        kafkaTemplate.send(
                KafkaTopics.RCA_COMPLETED,
                event.incidentKey(),
                event
        );
    }
}