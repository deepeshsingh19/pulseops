package com.pulseops.processor.evidence;

import com.pulseops.common.events.TelemetryEvent;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of the information available when an incident is investigated.
 *
 * The evidence object keeps the AI layer independent from Kafka,
 * Redis, Prometheus and Tempo implementation details.
 */
public record IncidentEvidence(
        String incidentKey,
        String serviceName,
        String title,
        String severity,
        Map<String, Object> metrics,
        List<TelemetryEvent> recentTelemetry,
        List<TraceEvidence> traces
) {
}