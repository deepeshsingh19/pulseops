package com.pulseops.common.events;

import java.time.Instant;
import java.util.Map;

public record TelemetryEvent(
        String eventId,
        String serviceName,
        EventType eventType,
        String severity,
        Instant timestamp,
        String message,
        Map<String, Object> metadata,
        String traceId,
        String spanId
) {

    public enum EventType {
        LOG,
        METRIC,
        TRACE,
        DEPLOYMENT,
        HEALTH_CHECK
    }
}