package com.pulseops.processor.evidence;

import java.time.Duration;

/**
 * Small, RCA-focused representation of a distributed trace.
 *
 * We don't pass the complete Tempo response to the AI service.
 * Only the information useful for explaining the incident is kept.
 */
public record TraceEvidence(
        String traceId,
        String serviceName,
        String operation,
        Duration duration,
        String status,
        String errorMessage
) {
}