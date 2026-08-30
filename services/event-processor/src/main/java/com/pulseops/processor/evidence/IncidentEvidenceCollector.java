package com.pulseops.processor.evidence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.pulseops.common.events.TelemetryEvent;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the evidence snapshot used by the RCA pipeline.
 *
 * Telemetry provides incident-specific signals, while Prometheus and
 * Tempo provide independent observability evidence.
 */
@Component
public class IncidentEvidenceCollector {

    private final PrometheusClient prometheusClient;
    private final TempoClient tempoClient;
    private final TelemetryCorrelationStoreAdapter telemetryStore;
    private final JsonMapper jsonMapper;

    public IncidentEvidenceCollector(
            PrometheusClient prometheusClient,
            TempoClient tempoClient,
            TelemetryCorrelationStoreAdapter telemetryStore,
            JsonMapper jsonMapper) {

        this.prometheusClient = prometheusClient;
        this.tempoClient = tempoClient;
        this.telemetryStore = telemetryStore;
        this.jsonMapper = jsonMapper;
    }

    public IncidentEvidence collect(
            String incidentKey,
            String serviceName,
            String title,
            String severity) {

        List<TelemetryEvent> telemetry =
                telemetryStore.getRecent(serviceName);

        Map<String, Object> metrics =
                collectMetrics(
                        serviceName,
                        telemetry
                );

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
            String serviceName,
            List<TelemetryEvent> telemetry) {

        Map<String, Object> metrics =
                new HashMap<>();

        /*
         * Incident-specific values are already available in the
         * correlation store, so use those as the primary RCA signals.
         */
        for (TelemetryEvent event : telemetry) {

            if (event.eventType()
                    != TelemetryEvent.EventType.METRIC) {

                continue;
            }

            if (event.metadata() == null) {
                continue;
            }

            Object metricValue =
                    event.metadata().get("metric");

            Object value =
                    event.metadata().get("value");

            if (metricValue == null
                    || !(value instanceof Number)) {

                continue;
            }

            metrics.put(
                    metricValue.toString(),
                    ((Number) value).doubleValue()
            );
        }

        /*
         * Prometheus confirms service availability and application-level
         * HTTP errors independently of the telemetry stream.
         */
        addPrometheusMetric(
                metrics,
                "service_up",
                "up{job=\"" + serviceName + "\"}"
        );

        addPrometheusMetric(
                metrics,
                "http_5xx_count",
                "http_server_requests_seconds_count"
                        + "{job=\""
                        + serviceName
                        + "\",outcome=\"SERVER_ERROR\"}"
        );

        return metrics;
    }

    private void addPrometheusMetric(
            Map<String, Object> metrics,
            String metricName,
            String query) {

        try {

            String response =
                    prometheusClient.query(query);

            JsonNode root =
                    jsonMapper.readTree(response);

            JsonNode results =
                    root.path("data")
                            .path("result");

            if (!results.isArray()
                    || results.isEmpty()) {

                return;
            }

            double total =
                    0.0;

            for (JsonNode result : results) {

                JsonNode value =
                        result.path("value");

                if (value.isArray()
                        && value.size() >= 2) {

                    String rawValue =
                            value.get(1).asText();

                    try {
                        total += Double.parseDouble(
                                rawValue
                        );
                    } catch (NumberFormatException ignored) {
                        // Ignore individual malformed samples.
                    }
                }
            }

            metrics.put(
                    metricName,
                    total
            );

        } catch (Exception exception) {

            /*
             * A Prometheus failure should not discard the telemetry
             * evidence already collected for the incident.
             */
            metrics.put(
                    metricName + "_error",
                    exception.getMessage()
            );
        }
    }
}