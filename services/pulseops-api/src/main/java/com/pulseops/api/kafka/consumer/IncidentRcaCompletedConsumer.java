package com.pulseops.api.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.incident.entity.IncidentRca;
import com.pulseops.api.incident.repository.IncidentRcaRepository;
import com.pulseops.api.incident.repository.IncidentRepository;
import com.pulseops.common.events.IncidentRcaCompletedEvent;

@Component
public class IncidentRcaCompletedConsumer {

    private final IncidentRepository incidentRepository;
    private final IncidentRcaRepository incidentRcaRepository;

    public IncidentRcaCompletedConsumer(
            IncidentRepository incidentRepository,
            IncidentRcaRepository incidentRcaRepository) {

        this.incidentRepository = incidentRepository;
        this.incidentRcaRepository =
                incidentRcaRepository;
    }

    @Transactional
    @KafkaListener(
            topics = "incidents.rca.completed",
            groupId = "pulseops-incident-rca-storage",
            containerFactory =
                    "incidentRcaCompletedKafkaListenerContainerFactory"
    )
    public void consume(
            IncidentRcaCompletedEvent event) {

        Incident incident =
                incidentRepository
                        .findById(event.incidentId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Incident not found: "
                                                + event.incidentId()
                                )
                        );

        IncidentRca rca =
                incidentRcaRepository
                        .findByIncidentId(
                                event.incidentId()
                        )
                        .orElseGet(
                                IncidentRca::new
                        );

        rca.setIncident(incident);
        rca.setRootCause(event.rootCause());
        rca.setConfidence(event.confidence());
        rca.setImpact(event.impact());

        rca.setEvidence(
                String.join(
                        "\n",
                        event.evidence()
                )
        );

        rca.setRecommendedActions(
                String.join(
                        "\n",
                        event.recommendedActions()
                )
        );

        incidentRcaRepository.save(rca);
    }
}