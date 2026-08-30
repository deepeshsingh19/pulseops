package com.pulseops.common.events;

import java.time.Instant;

/**
 * Requests an RCA investigation for an incident that has already
 * been persisted by the incident service.
 */
public record RcaRequestedEvent(
        Long incidentId,
        String incidentKey,
        String serviceName,
        String title,
        String severity,
        String description,
        Instant requestedAt
) {
}