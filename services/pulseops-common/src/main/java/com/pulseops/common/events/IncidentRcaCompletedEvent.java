package com.pulseops.common.events;

import java.time.Instant;
import java.util.List;

/**
 * Published after the RCA engine has completed its investigation.
 */
public record IncidentRcaCompletedEvent(
        Long incidentId,
        String incidentKey,
        String rootCause,
        double confidence,
        String impact,
        List<String> evidence,
        List<String> recommendedActions,
        Instant completedAt
) {
}