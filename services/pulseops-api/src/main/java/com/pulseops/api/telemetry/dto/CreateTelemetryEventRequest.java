package com.pulseops.api.telemetry.dto;

import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * REST representation of incoming telemetry.
 *
 * Example:
 *
 * {
 *   "eventId": "evt-1001",
 *   "serviceName": "payment-service",
 *   "eventType": "METRIC",
 *   "severity": "INFO",
 *   "timestamp": "...",
 *   "message": "Database connection pool usage",
 *   "metadata": {
 *      "metric": "db_pool_usage",
 *      "value": 92
 *   }
 * }
 */
public record CreateTelemetryEventRequest(

        @NotBlank
        @Size(max = 100)
        String eventId,

        @NotBlank
        @Size(max = 100)
        String serviceName,

        @NotBlank
        String eventType,

        @NotBlank
        String severity,

        @NotNull
        Instant timestamp,

        String message,

        Map<String, Object> metadata,

        String traceId,

        String spanId,

        String source
) {
}