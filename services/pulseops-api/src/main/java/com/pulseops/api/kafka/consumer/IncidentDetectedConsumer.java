package com.pulseops.api.kafka.consumer;

import com.pulseops.api.incident.service.IncidentService;
import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.api.kafka.config.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentDetectedConsumer {

    private final IncidentService incidentService;

    public IncidentDetectedConsumer(
            IncidentService incidentService) {

        this.incidentService = incidentService;
    }

    @KafkaListener(
            topics = KafkaTopics.INCIDENTS_DETECTED,
            groupId = "pulseops-incident-creator"
    )
    public void consume(IncidentDetectedEvent event) {

        incidentService.createIncidentFromDetection(event);
    }
}