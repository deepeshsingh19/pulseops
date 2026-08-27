package com.pulseops.api.incident.dto;

import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.incident.entity.IncidentSeverity;
import com.pulseops.api.incident.entity.IncidentStatus;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String incidentKey,
        String title,
        String description,
        IncidentSeverity severity,
        IncidentStatus status,
        String serviceName,
        Instant detectedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentKey(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getServiceName(),
                incident.getDetectedAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}