package com.pulseops.processor.correlation;

import com.pulseops.common.events.IncidentDetectedEvent;
import com.pulseops.common.events.TelemetryEvent;
import com.pulseops.processor.ai.AiServiceClient;
import com.pulseops.processor.ai.AnomalyResponse;
import com.pulseops.processor.evidence.IncidentEvidence;
import com.pulseops.processor.evidence.IncidentEvidenceCollector;
import com.pulseops.processor.kafka.producer.IncidentDetectedProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryCorrelationEngineTest {

    @Mock
    private TelemetryCorrelationStore correlationStore;

    @Mock
    private IncidentDetectedProducer incidentDetectedProducer;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private IncidentEvidenceCollector evidenceCollector;

    private TelemetryCorrelationEngine engine;

    @BeforeEach
    void setUp() {

        engine = new TelemetryCorrelationEngine(
                correlationStore,
                incidentDetectedProducer,
                redisTemplate,
                aiServiceClient,
                evidenceCollector
        );
    }

    @Test
    void shouldCreateIncidentWhenAllDatabaseSignalsArePresent() {

        Instant now = Instant.now();

        List<TelemetryEvent> events = List.of(
                metricEvent(
                        "db_latency_ms",
                        1500,
                        now.minusSeconds(10)
                ),
                metricEvent(
                        "db_pool_usage",
                        95,
                        now.minusSeconds(5)
                ),
                metricEvent(
                        "http_5xx_rate",
                        12,
                        now
                )
        );

        when(correlationStore.getRecent("payment-service"))
                .thenReturn(events);

        when(redisTemplate.hasKey(
                "pulseops:incident-cooldown:payment-service"
        )).thenReturn(false);

        when(aiServiceClient.score(any(TelemetryEvent.class)))
                .thenReturn(
                        new AnomalyResponse(
                                "payment-service",
                                0.80,
                                false,
                                true
                        )
                );

        when(evidenceCollector.collect(
                anyString(),
                eq("payment-service"),
                anyString(),
                eq("CRITICAL")
        )).thenReturn(
                new IncidentEvidence(
                        "AUTO-TEST",
                        "payment-service",
                        "Payment service degradation detected",
                        "CRITICAL",
                        Map.of(),
                        events,
                        List.of()
                )
        );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        engine.process(events.get(2));

        ArgumentCaptor<IncidentDetectedEvent> captor =
                ArgumentCaptor.forClass(
                        IncidentDetectedEvent.class
                );

        verify(incidentDetectedProducer)
                .publish(captor.capture());

        IncidentDetectedEvent incident =
                captor.getValue();

        assertEquals(
                "payment-service",
                incident.serviceName()
        );

        assertEquals(
                "CRITICAL",
                incident.severity()
        );

        assertTrue(
                incident.evidence().stream()
                        .anyMatch(
                                item ->
                                        item.contains(
                                                "Database latency exceeded"
                                        )
                        )
        );

        verify(valueOperations).set(
                eq("pulseops:incident-cooldown:payment-service"),
                eq("1"),
                any(java.time.Duration.class)
        );
    }

    @Test
    void shouldNotCreateIncidentWhenOneRequiredSignalIsMissing() {

        Instant now = Instant.now();

        List<TelemetryEvent> events = List.of(
                metricEvent(
                        "db_latency_ms",
                        1500,
                        now.minusSeconds(10)
                ),
                metricEvent(
                        "db_pool_usage",
                        95,
                        now
                )
        );

        when(correlationStore.getRecent("payment-service"))
                .thenReturn(events);

        when(aiServiceClient.score(any(TelemetryEvent.class)))
                .thenReturn(
                        new AnomalyResponse(
                                "payment-service",
                                0.20,
                                false,
                                true
                        )
                );

        engine.process(events.get(1));

        verify(
                incidentDetectedProducer,
                never()
        ).publish(any());

        verify(
                evidenceCollector,
                never()
        ).collect(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldCreateIncidentFromStrongMlSignalWithSupportingEvidence() {

        Instant now = Instant.now();

        List<TelemetryEvent> events = List.of(
                metricEvent(
                        "db_latency_ms",
                        1500,
                        now
                )
        );

        when(correlationStore.getRecent("payment-service"))
                .thenReturn(events);

        when(redisTemplate.hasKey(
                "pulseops:incident-cooldown:payment-service"
        )).thenReturn(false);

        when(aiServiceClient.score(any(TelemetryEvent.class)))
                .thenReturn(
                        new AnomalyResponse(
                                "payment-service",
                                0.92,
                                true,
                                true
                        )
                );

        when(evidenceCollector.collect(
                anyString(),
                eq("payment-service"),
                anyString(),
                eq("CRITICAL")
        )).thenReturn(
                new IncidentEvidence(
                        "AUTO-TEST",
                        "payment-service",
                        "Payment service degradation detected",
                        "CRITICAL",
                        Map.of(),
                        events,
                        List.of()
                )
        );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        engine.process(events.get(0));

        verify(incidentDetectedProducer)
                .publish(
                        any(IncidentDetectedEvent.class)
                );

        verify(valueOperations).set(
                eq("pulseops:incident-cooldown:payment-service"),
                eq("1"),
                any(java.time.Duration.class)
        );
    }

    @Test
    void shouldNotCreateDuplicateIncidentDuringCooldown() {

        Instant now = Instant.now();

        List<TelemetryEvent> events = List.of(
                metricEvent(
                        "db_latency_ms",
                        1500,
                        now.minusSeconds(10)
                ),
                metricEvent(
                        "db_pool_usage",
                        95,
                        now.minusSeconds(5)
                ),
                metricEvent(
                        "http_5xx_rate",
                        12,
                        now
                )
        );

        when(correlationStore.getRecent("payment-service"))
                .thenReturn(events);

        when(redisTemplate.hasKey(
                "pulseops:incident-cooldown:payment-service"
        )).thenReturn(true);

        when(aiServiceClient.score(any(TelemetryEvent.class)))
                .thenReturn(
                        new AnomalyResponse(
                                "payment-service",
                                0.90,
                                false,
                                true
                        )
                );

        engine.process(events.get(2));

        verify(
                incidentDetectedProducer,
                never()
        ).publish(any());

        verify(
                evidenceCollector,
                never()
        ).collect(
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shouldContinueWithRuleBasedDetectionWhenAiServiceFails() {

        Instant now = Instant.now();

        List<TelemetryEvent> events = List.of(
                metricEvent(
                        "db_latency_ms",
                        1500,
                        now.minusSeconds(10)
                ),
                metricEvent(
                        "db_pool_usage",
                        95,
                        now.minusSeconds(5)
                ),
                metricEvent(
                        "http_5xx_rate",
                        12,
                        now
                )
        );

        when(correlationStore.getRecent("payment-service"))
                .thenReturn(events);

        when(redisTemplate.hasKey(
                "pulseops:incident-cooldown:payment-service"
        )).thenReturn(false);

        when(aiServiceClient.score(any(TelemetryEvent.class)))
                .thenThrow(
                        new RuntimeException(
                                "AI service unavailable"
                        )
                );

        when(evidenceCollector.collect(
                anyString(),
                eq("payment-service"),
                anyString(),
                eq("CRITICAL")
        )).thenReturn(
                new IncidentEvidence(
                        "AUTO-TEST",
                        "payment-service",
                        "Payment service degradation detected",
                        "CRITICAL",
                        Map.of(),
                        events,
                        List.of()
                )
        );

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        engine.process(events.get(2));

        verify(incidentDetectedProducer)
                .publish(
                        any(IncidentDetectedEvent.class)
                );

        verify(valueOperations).set(
                eq("pulseops:incident-cooldown:payment-service"),
                eq("1"),
                any(java.time.Duration.class)
        );
    }

    private TelemetryEvent metricEvent(
            String metric,
            double value,
            Instant timestamp) {

        return new TelemetryEvent(
                "event-" + metric + "-" + timestamp.toEpochMilli(),
                "payment-service",
                TelemetryEvent.EventType.METRIC,
                "WARN",
                timestamp,
                metric,
                Map.of(
                        "metric",
                        metric,
                        "value",
                        value
                ),
                null,
                null
        );
    }
}