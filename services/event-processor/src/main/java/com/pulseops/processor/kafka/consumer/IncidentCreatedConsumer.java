package com.pulseops.processor.kafka.consumer;

import com.pulseops.common.events.IncidentCreatedEvent;
import com.pulseops.processor.kafka.config.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentCreatedConsumer {

    @KafkaListener(
            topics = KafkaTopics.INCIDENTS_CREATED,
            groupId = "pulseops-event-processor",
            containerFactory = "incidentCreatedKafkaListenerContainerFactory"
    )
    public void consume(IncidentCreatedEvent event) {

        System.out.println(
                "Received incident event: " +
                event.incidentKey() +
                " | service=" + event.serviceName() +
                " | severity=" + event.severity()
        );
    }
}