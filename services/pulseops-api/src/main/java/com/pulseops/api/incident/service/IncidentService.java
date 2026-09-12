package com.pulseops.api.incident.service;

import java.time.Instant;
import java.util.List;
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
import com.pulseops.api.telemetry.entity.TelemetryEventEntity;
import com.pulseops.api.telemetry.repository.TelemetryEventRepository;
import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.common.events.RcaRequestedEvent;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class IncidentService {

    private static final long TELEMETRY_WINDOW_SECONDS = 60;

    private final IncidentRepository incidentRepository;
    private final IncidentRcaRepository incidentRcaRepository;
    private final TelemetryEventRepository telemetryEventRepository;
    private final OutboxService outboxService;
    private final RcaRequestedProducer rcaRequestedProducer;
    private final ObjectMapper objectMapper;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentRcaRepository incidentRcaRepository,
            TelemetryEventRepository telemetryEventRepository,
            OutboxService outboxService,
            RcaRequestedProducer rcaRequestedProducer,
            ObjectMapper objectMapper) {

        this.incidentRepository = incidentRepository;
        this.incidentRcaRepository = incidentRcaRepository;
        this.telemetryEventRepository = telemetryEventRepository;
        this.outboxService = outboxService;
        this.rcaRequestedProducer = rcaRequestedProducer;
        this.objectMapper = objectMapper;
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
    public List<IncidentResponse> getIncidents() {

        return incidentRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(IncidentResponse::from)
                .toList();
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

        List<IncidentDetailResponse.TelemetryResponse>
                telemetry =
                getTelemetry(
                        incident
                );

        return incidentRcaRepository
                .findByIncidentId(incidentId)
                .map(rca ->
                        IncidentDetailResponse.from(
                                incident,
                                rca,
                                telemetry
                        )
                )
                .orElseGet(() ->
                        IncidentDetailResponse.from(
                                incident,
                                null,
                                telemetry
                        )
                );
    }

    private List<IncidentDetailResponse.TelemetryResponse>
    getTelemetry(Incident incident) {

        Instant detectedAt = incident.getDetectedAt();

        if (detectedAt == null) {
            return List.of();
        }

        Instant start =
                detectedAt.minusSeconds(
                        TELEMETRY_WINDOW_SECONDS
                );

        Instant end = detectedAt;

        return telemetryEventRepository
                .findByServiceNameAndEventTimestampBetweenOrderByEventTimestampAsc(
                        incident.getServiceName(),
                        start,
                        end
                )
                .stream()
                .filter(event ->
                        "METRIC".equalsIgnoreCase(
                                event.getEventType()
                        )
                )
                .map(this::toTelemetryResponse)
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private java.util.Optional<
            IncidentDetailResponse.TelemetryResponse>
    toTelemetryResponse(
            TelemetryEventEntity event) {

        if (event.getMetadata() == null
                || event.getMetadata().isBlank()) {

            return java.util.Optional.empty();
        }

        try {

            JsonNode metadata =
                    objectMapper.readTree(
                            event.getMetadata()
                    );

            JsonNode metricNode =
                    metadata.get("metric");

            JsonNode valueNode =
                    metadata.get("value");

            if (metricNode == null
                    || valueNode == null
                    || !valueNode.isNumber()) {

                return java.util.Optional.empty();
            }

            return java.util.Optional.of(
                    new IncidentDetailResponse.TelemetryResponse(
                            metricNode.asText(),
                            valueNode.asDouble(),
                            event.getEventTimestamp(),
                            event.getTraceId(),
                            event.getSpanId()
                    )
            );

        } catch (Exception exception) {

            return java.util.Optional.empty();
        }
    }

    private String generateIncidentKey() {

        return "INC-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}