package com.pulseops.api.incident.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.api.incident.dto.CreateIncidentRequest;
import com.pulseops.api.incident.dto.IncidentDetailResponse;
import com.pulseops.api.incident.dto.IncidentResponse;
import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.incident.entity.IncidentSeverity;
import com.pulseops.api.incident.entity.IncidentStatus;
import com.pulseops.api.incident.repository.IncidentRcaRepository;
import com.pulseops.api.incident.repository.IncidentRepository;
import com.pulseops.api.kafka.producer.RcaRequestedProducer;
import com.pulseops.api.outbox.service.OutboxService;
import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.common.events.RcaRequestedEvent;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentRcaRepository incidentRcaRepository;
    private final OutboxService outboxService;
    private final RcaRequestedProducer rcaRequestedProducer;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentRcaRepository incidentRcaRepository,
            OutboxService outboxService,
            RcaRequestedProducer rcaRequestedProducer) {

        this.incidentRepository = incidentRepository;
        this.incidentRcaRepository = incidentRcaRepository;
        this.outboxService = outboxService;
        this.rcaRequestedProducer = rcaRequestedProducer;
    }

    @Transactional
    public IncidentResponse createIncident(
            CreateIncidentRequest request) {

        Incident incident = new Incident();

        incident.setIncidentKey(
                generateIncidentKey()
        );

        incident.setTitle(request.title());
        incident.setDescription(request.description());
        incident.setSeverity(request.severity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setServiceName(request.serviceName());

        Incident savedIncident =
                incidentRepository.save(incident);

        outboxService.createIncidentCreatedEvent(
                savedIncident
        );

        return IncidentResponse.from(
                savedIncident
        );
    }

    @Transactional
    public void createIncidentFromDetection(
            IncidentDetectedEvent event) {

        Incident incident = new Incident();

        incident.setIncidentKey(
                event.incidentKey()
        );

        incident.setTitle(
                event.title()
        );

        incident.setDescription(
                event.description()
        );

        incident.setSeverity(
                IncidentSeverity.valueOf(
                        event.severity()
                )
        );

        incident.setStatus(
                IncidentStatus.OPEN
        );

        incident.setServiceName(
                event.serviceName()
        );

        incident.setDetectedAt(
                event.detectedAt()
        );

        Incident savedIncident =
                incidentRepository.save(incident);

        outboxService.createIncidentCreatedEvent(
                savedIncident
        );

        /*
         * The incident is persisted before requesting RCA so the
         * investigation can reference the database identifier.
         */
        rcaRequestedProducer.publish(
                new RcaRequestedEvent(
                        savedIncident.getId(),
                        savedIncident.getIncidentKey(),
                        savedIncident.getServiceName(),
                        savedIncident.getTitle(),
                        savedIncident.getSeverity().name(),
                        savedIncident.getDescription(),
                        Instant.now()
                )
        );
    }

    @Transactional(readOnly = true)
    public IncidentDetailResponse getIncident(
            Long incidentId) {

        Incident incident =
                incidentRepository.findById(
                        incidentId
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "Incident not found: "
                                        + incidentId
                        )
                );

        return incidentRcaRepository
                .findByIncidentId(incidentId)
                .map(rca ->
                        IncidentDetailResponse.from(
                                incident,
                                rca
                        )
                )
                .orElseGet(() ->
                        new IncidentDetailResponse(
                                incident.getId(),
                                incident.getIncidentKey(),
                                incident.getTitle(),
                                incident.getDescription(),
                                incident.getSeverity().name(),
                                incident.getStatus().name(),
                                incident.getServiceName(),
                                incident.getDetectedAt(),
                                incident.getCreatedAt(),
                                incident.getUpdatedAt(),
                                null
                        )
                );
    }

    private String generateIncidentKey() {

        return "INC-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}