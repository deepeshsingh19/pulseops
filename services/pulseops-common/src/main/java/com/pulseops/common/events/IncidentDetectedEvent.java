package com.pulseops.common.events;

import java.time.Instant;
import java.util.List;

/**
 * Published when the correlation engine determines that
 * multiple telemetry signals represent a likely incident.
 */
public record IncidentDetectedEvent(
        String incidentKey,
        String serviceName,
        String title,
        String severity,
        String description,
        double confidence,
        List<String> evidence,
        Instant detectedAt
) {
}