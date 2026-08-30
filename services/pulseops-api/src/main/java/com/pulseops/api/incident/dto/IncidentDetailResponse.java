package com.pulseops.api.incident.dto;

import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.incident.entity.IncidentRca;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record IncidentDetailResponse(
        Long id,
        String incidentKey,
        String title,
        String description,
        String severity,
        String status,
        String serviceName,
        Instant detectedAt,
        Instant createdAt,
        Instant updatedAt,
        RcaResponse rca
) {

    public static IncidentDetailResponse from(
            Incident incident,
            IncidentRca rca) {

        return new IncidentDetailResponse(
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
                RcaResponse.from(rca)
        );
    }

    public record RcaResponse(
            String rootCause,
            double confidence,
            String impact,
            List<String> evidence,
            List<String> recommendedActions
    ) {

        private static RcaResponse from(
                IncidentRca rca) {

            return new RcaResponse(
                    rca.getRootCause(),
                    rca.getConfidence(),
                    rca.getImpact(),
                    splitLines(rca.getEvidence()),
                    splitLines(rca.getRecommendedActions())
            );
        }

        private static List<String> splitLines(
                String value) {

            if (value == null || value.isBlank()) {
                return List.of();
            }

            return Arrays.stream(
                            value.split("\\R")
                    )
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();
        }
    }
}