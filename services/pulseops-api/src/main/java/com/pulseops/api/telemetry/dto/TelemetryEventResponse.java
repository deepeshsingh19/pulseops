package com.pulseops.api.telemetry.dto;

import java.time.Instant;

import com.pulseops.api.telemetry.entity.TelemetryEventEntity;

public record TelemetryEventResponse(
        Long id,
        String eventId,
        String serviceName,
        String eventType,
        String severity,
        Instant timestamp,
        String message,
        String traceId,
        String spanId,
        String source,
        Instant createdAt
) {

    public static TelemetryEventResponse from(
            TelemetryEventEntity event) {

        return new TelemetryEventResponse(
                event.getId(),
                event.getEventId(),
                event.getServiceName(),
                event.getEventType(),
                event.getSeverity(),
                event.getEventTimestamp(),
                event.getMessage(),
                event.getTraceId(),
                event.getSpanId(),
                event.getSource(),
                event.getCreatedAt()
        );
    }
}