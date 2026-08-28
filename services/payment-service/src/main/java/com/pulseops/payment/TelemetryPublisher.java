package com.pulseops.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class TelemetryPublisher {

    private final RestClient restClient;
    private final PaymentController paymentController;

    public TelemetryPublisher(
            @Value("${pulseops.api.base-url}") String baseUrl,
            PaymentController paymentController) {

        this.restClient = RestClient.create(baseUrl);
        this.paymentController = paymentController;
    }

    @Scheduled(fixedRate = 10000)
    public void publishTelemetry() {

        FailureMode mode =
                paymentController.getCurrentFailureMode();

        double latency = switch (mode) {
            case NORMAL -> 100 + Math.random() * 50;
            case LATENCY -> 1500 + Math.random() * 500;
            case ERROR -> 1400 + Math.random() * 400;
        };

        double poolUsage = switch (mode) {
            case NORMAL -> 35 + Math.random() * 10;
            case LATENCY -> 92 + Math.random() * 4;
            case ERROR -> 97 + Math.random() * 3;
        };

        double errorRate = switch (mode) {
            case NORMAL -> 0.5 + Math.random();
            case LATENCY -> 3 + Math.random() * 2;
            case ERROR -> 12 + Math.random() * 8;
        };

        publish("db_latency_ms", latency, "WARN");
        publish("db_pool_usage", poolUsage, "WARN");
        publish("http_5xx_rate", errorRate, "CRITICAL");
    }

    private void publish(
            String metric,
            double value,
            String severity) {

        Map<String, Object> request = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "serviceName", "payment-service",
                "eventType", "METRIC",
                "severity", severity,
                "timestamp", Instant.now().toString(),
                "message", metric,
                "metadata", Map.of(
                        "metric", metric,
                        "value", value
                ),
                "source", "payment-service"
        );

        try {
            restClient
                    .post()
                    .uri("/api/v1/telemetry/events")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception exception) {

            // Telemetry is best-effort; payment processing must remain independent of PulseOps.
            System.err.println(
                    "Failed to publish telemetry: "
                            + exception.getMessage()
            );
        }
    }
}