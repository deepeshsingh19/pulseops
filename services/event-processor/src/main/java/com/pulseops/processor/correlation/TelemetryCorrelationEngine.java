package com.pulseops.processor.correlation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.common.events.TelemetryEvent;
import com.pulseops.processor.ai.AiServiceClient;
import com.pulseops.processor.ai.AnomalyResponse;
import com.pulseops.processor.evidence.IncidentEvidence;
import com.pulseops.processor.evidence.IncidentEvidenceCollector;
import com.pulseops.processor.kafka.producer.IncidentDetectedProducer;

@Service
public class TelemetryCorrelationEngine {

    private static final Duration WINDOW =
            Duration.ofSeconds(60);

    private static final Duration INCIDENT_COOLDOWN =
            Duration.ofMinutes(5);

    private final TelemetryCorrelationStore correlationStore;
    private final IncidentDetectedProducer incidentDetectedProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final AiServiceClient aiServiceClient;
    private final IncidentEvidenceCollector evidenceCollector;

    public TelemetryCorrelationEngine(
            TelemetryCorrelationStore correlationStore,
            IncidentDetectedProducer incidentDetectedProducer,
            RedisTemplate<String, String> redisTemplate,
            AiServiceClient aiServiceClient,
            IncidentEvidenceCollector evidenceCollector) {

        this.correlationStore = correlationStore;
        this.incidentDetectedProducer =
                incidentDetectedProducer;
        this.redisTemplate = redisTemplate;
        this.aiServiceClient = aiServiceClient;
        this.evidenceCollector = evidenceCollector;
    }

    public void process(TelemetryEvent event) {

        correlationStore.add(
                event.serviceName(),
                event
        );

        List<TelemetryEvent> events =
                correlationStore.getRecent(
                        event.serviceName()
                );

        List<TelemetryEvent> recentEvents =
                events.stream()
                        .filter(existing ->
                                Duration.between(
                                        existing.timestamp(),
                                        event.timestamp()
                                ).compareTo(WINDOW) <= 0
                        )
                        .toList();

        AnomalyResponse anomaly = null;

        try {

            anomaly =
                    aiServiceClient.score(event);

        } catch (Exception exception) {

            /*
             * ML is an additional signal, not a hard dependency.
             * Rule-based correlation can still detect an incident
             * when the AI service is temporarily unavailable.
             */
            System.err.println(
                    "AI service unavailable: "
                            + exception.getMessage()
            );

            if (exception.getCause() != null) {

                System.err.println(
                        "Cause: "
                                + exception.getCause()
                                        .getMessage()
                );
            }
        }

        evaluatePaymentDatabaseFailure(
                event.serviceName(),
                recentEvents,
                event.timestamp(),
                anomaly
        );
    }

    private void evaluatePaymentDatabaseFailure(
            String serviceName,
            List<TelemetryEvent> events,
            Instant now,
            AnomalyResponse anomaly) {

        boolean highDbLatency = false;
        boolean highPoolUsage = false;
        boolean highErrorRate = false;

        List<String> evidence =
                new ArrayList<>();

        for (TelemetryEvent event : events) {

            if (event.eventType()
                    != TelemetryEvent.EventType.METRIC) {

                continue;
            }

            String metric =
                    extractMetric(event);

            double value =
                    numericValue(event);

            if ("db_latency_ms".equals(metric)
                    && value > 1000) {

                highDbLatency = true;

                evidence.add(
                        "Database latency exceeded 1000 ms"
                );
            }

            if ("db_pool_usage".equals(metric)
                    && value > 90) {

                highPoolUsage = true;

                evidence.add(
                        "Database connection pool exceeded 90%"
                );
            }

            if ("http_5xx_rate".equals(metric)
                    && value > 5) {

                highErrorRate = true;

                evidence.add(
                        "HTTP 5xx rate exceeded 5%"
                );
            }
        }

        boolean rulesTriggered =
                highDbLatency
                        && highPoolUsage
                        && highErrorRate;

        boolean mlTriggered =
                anomaly != null
                        && anomaly.anomalous();

        /*
         * Require either the complete deterministic signal set
         * or an ML anomaly combined with meaningful evidence.
         */
        boolean incidentDetected =
                rulesTriggered
                        || (mlTriggered
                        && (highDbLatency
                        || highPoolUsage
                        || highErrorRate));

        if (!incidentDetected) {
            return;
        }

        if (inCooldown(serviceName)) {
            return;
        }

        if (anomaly != null) {

            evidence.add(
                    String.format(
                            "ML anomaly score: %.2f",
                            anomaly.anomalyScore()
                    )
            );
        }

        String incidentKey =
                generateIncidentKey(serviceName);

        /*
         * Collect operational evidence only after an incident has
         * actually been detected. This avoids constantly querying
         * Prometheus and Tempo for normal telemetry.
         */
        try {

            IncidentEvidence incidentEvidence =
                    evidenceCollector.collect(
                            incidentKey,
                            serviceName,
                            "Payment service degradation detected",
                            "CRITICAL"
                    );

            evidence.addAll(
                    buildEvidenceSummary(
                            incidentEvidence
                    )
            );

        } catch (Exception exception) {

            /*
             * Evidence collection should never prevent an incident
             * from being published. The original correlation evidence
             * is still valuable if an observability backend is down.
             */
            evidence.add(
                    "Additional evidence collection failed: "
                            + exception.getMessage()
            );
        }

        IncidentDetectedEvent incident =
                new IncidentDetectedEvent(
                        incidentKey,
                        serviceName,
                        "Payment service degradation detected",
                        "CRITICAL",
                        "Telemetry correlation detected abnormal payment-service behavior.",
                        calculateConfidence(anomaly),
                        evidence,
                        now
                );

        incidentDetectedProducer.publish(
                incident
        );

        setCooldown(serviceName);
    }

    private List<String> buildEvidenceSummary(
            IncidentEvidence incidentEvidence) {

        List<String> summary =
                new ArrayList<>();

        /*
         * Keep the Kafka event compact. The detailed evidence object
         * will be sent to the RCA service in the next stage.
         */
        summary.add(
                "Recent telemetry events: "
                        + incidentEvidence.recentTelemetry().size()
        );

        summary.add(
                "Prometheus metrics collected"
        );

        summary.add(
                "Tempo traces collected: "
                        + incidentEvidence.traces().size()
        );

        for (var trace :
                incidentEvidence.traces()) {

            summary.add(
                    String.format(
                            "Trace %s: %s (%d ms)",
                            trace.traceId(),
                            trace.operation(),
                            trace.duration().toMillis()
                    )
            );
        }

        return summary;
    }

    private double calculateConfidence(
            AnomalyResponse anomaly) {

        if (anomaly == null) {
            return 0.90;
        }

        return Math.min(
                0.99,
                0.70
                        + (anomaly.anomalyScore() * 0.30)
        );
    }

    private boolean inCooldown(
            String serviceName) {

        String key =
                "pulseops:incident-cooldown:"
                        + serviceName;

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }

    private void setCooldown(
            String serviceName) {

        String key =
                "pulseops:incident-cooldown:"
                        + serviceName;

        redisTemplate.opsForValue().set(
                key,
                "1",
                INCIDENT_COOLDOWN
        );
    }

    private String extractMetric(
            TelemetryEvent event) {

        if (event.metadata() == null) {
            return "";
        }

        Object metric =
                event.metadata().get("metric");

        return metric == null
                ? ""
                : metric.toString();
    }

    private double numericValue(
            TelemetryEvent event) {

        if (event.metadata() == null) {
            return 0;
        }

        Object value =
                event.metadata().get("value");

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return 0;
    }

    private String generateIncidentKey(
            String serviceName) {

        return "AUTO-"
                + serviceName.toUpperCase()
                + "-"
                + UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}