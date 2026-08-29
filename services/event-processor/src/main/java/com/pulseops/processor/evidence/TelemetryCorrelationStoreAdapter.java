package com.pulseops.processor.evidence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pulseops.common.events.TelemetryEvent;
import com.pulseops.processor.correlation.TelemetryCorrelationStore;

/**
 * Keeps the evidence layer independent from the correlation package.
 */
@Component
public class TelemetryCorrelationStoreAdapter {

    private final TelemetryCorrelationStore correlationStore;

    public TelemetryCorrelationStoreAdapter(
            TelemetryCorrelationStore correlationStore) {

        this.correlationStore = correlationStore;
    }

    public List<TelemetryEvent> getRecent(
            String serviceName) {

        return correlationStore.getRecent(serviceName);
    }
}