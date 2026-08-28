package com.pulseops.order;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TelemetryPublisher {

    private final RestClient restClient;

    public TelemetryPublisher(
            @Value("${pulseops.api.base-url}") String baseUrl) {

        this.restClient = RestClient
                .create(baseUrl);
    }

    @Scheduled(fixedRate = 10000)
    public void publishTelemetry() {

        publish(
                "cpu_usage",
                35.0 + Math.random() * 15
        );
    }

    private void publish(
            String metric,
            double value) {

        Map<String, Object> request = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "serviceName", "order-service",
                "eventType", "METRIC",
                "severity", "INFO",
                "timestamp", Instant.now().toString(),
                "message", metric,
                "metadata", Map.of(
                        "metric", metric,
                        "value", value
                ),
                "source", "order-service"
        );

        try {
            restClient
                    .post()
                    .uri("/api/v1/telemetry/events")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception exception) {
            // Monitoring must not stop the business service when PulseOps is unavailable.
            System.err.println(
                    "Failed to publish telemetry: "
                            + exception.getMessage()
            );
        }
    }
}