package com.pulseops.processor.kafka.consumer;

import com.pulseops.common.events.TelemetryEvent;
import com.pulseops.processor.correlation.TelemetryCorrelationEngine;
import com.pulseops.processor.kafka.config.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryEventConsumer {

    private final TelemetryCorrelationEngine correlationEngine;

    public TelemetryEventConsumer(
            TelemetryCorrelationEngine correlationEngine) {

        this.correlationEngine = correlationEngine;
    }

    @KafkaListener(
            topics = KafkaTopics.TELEMETRY_EVENTS,
            groupId = "pulseops-telemetry-processor",
            containerFactory = "telemetryKafkaListenerContainerFactory"
    )
    public void consume(TelemetryEvent event) {

        correlationEngine.process(event);
    }
}