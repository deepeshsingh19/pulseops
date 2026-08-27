package com.pulseops.processor.ai;

import com.pulseops.common.events.TelemetryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiServiceClient {

    private final RestClient restClient;

    public AiServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${pulseops.ai.base-url}") String baseUrl) {

        /*
         * Spring Boot provides the configured RestClient.Builder.
         * We only add the AI service's base URL here.
         */
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public AnomalyResponse score(TelemetryEvent event) {

        AnomalyRequest request = new AnomalyRequest(
                event.serviceName(),
                event.metadata()
        );

        /*
         * RestClient uses Spring's HTTP message converters to serialize
         * the request object as JSON and deserialize the response.
         */
        AnomalyResponse response = restClient
                .post()
                .uri("/api/v1/anomaly/score")
                .body(request)
                .retrieve()
                .body(AnomalyResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "AI service returned an empty response"
            );
        }

        return response;
    }
}