package com.pulseops.processor.evidence;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.pulseops.common.events.TelemetryEvent;

/**
 * Builds the evidence snapshot used by the RCA pipeline.
 *
 * This component gathers evidence but does not interpret it.
 */
@Component
public class IncidentEvidenceCollector {

    private final PrometheusClient prometheusClient;
    private final TempoClient tempoClient;
    private final TelemetryCorrelationStoreAdapter telemetryStore;

    public IncidentEvidenceCollector(
            PrometheusClient prometheusClient,
            TempoClient tempoClient,
            TelemetryCorrelationStoreAdapter telemetryStore) {

        this.prometheusClient = prometheusClient;
        this.tempoClient = tempoClient;
        this.telemetryStore = telemetryStore;
    }

    public IncidentEvidence collect(
            String incidentKey,
            String serviceName,
            String title,
            String severity) {

        List<TelemetryEvent> telemetry =
                telemetryStore.getRecent(serviceName);

        Map<String, Object> metrics =
                collectMetrics(serviceName);

        List<TraceEvidence> traces =
                tempoClient.searchRecentTraces(
                        serviceName
                );

        return new IncidentEvidence(
                incidentKey,
                serviceName,
                title,
                severity,
                metrics,
                telemetry,
                traces
        );
    }

    private Map<String, Object> collectMetrics(
            String serviceName) {

        String query =
                "http_server_requests_seconds_count{service=\""
                        + serviceName
                        + "\"}";

        String response =
                prometheusClient.query(query);

        return Map.of(
                "prometheusQuery", query,
                "prometheusResponse", response
        );
    }
}