package com.pulseops.common.events;

import java.time.Instant;

/**
 * Event published whenever a new incident is successfully created.
 *
 * This class belongs in pulseops-common because multiple services
 * will eventually need to understand the same Kafka event contract.
 */
public record IncidentCreatedEvent(
        Long incidentId,
        String incidentKey,
        String title,
        String severity,
        String status,
        String serviceName,
        Instant createdAt
) {
}