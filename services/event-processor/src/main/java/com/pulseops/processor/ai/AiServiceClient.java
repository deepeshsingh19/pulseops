package com.pulseops.processor.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pulseops.common.events.TelemetryEvent;

import tools.jackson.databind.json.JsonMapper;

@Component
public class AiServiceClient {

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String baseUrl;

    public AiServiceClient(
            JsonMapper jsonMapper,
            @Value("${pulseops.ai.base-url}") String baseUrl) {

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.jsonMapper = jsonMapper;
        this.baseUrl = baseUrl;
    }

    public AnomalyResponse score(TelemetryEvent event) {

        AnomalyRequest request = new AnomalyRequest(
                event.serviceName(),
                event.metadata()
        );

        try {
            String jsonBody =
                    jsonMapper.writeValueAsString(request);

            /*
             * Java HttpClient manages Content-Length automatically.
             * Only the application-level headers are set here.
             */
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl + "/api/v1/anomaly/score"
                    ))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header(
                            "Content-Type",
                            "application/json"
                    )
                    .header(
                            "Accept",
                            "application/json"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    jsonBody,
                                    StandardCharsets.UTF_8
                            )
                    )
                    .build();

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "AI service returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            AnomalyResponse anomalyResponse =
                    jsonMapper.readValue(
                            response.body(),
                            AnomalyResponse.class
                    );

            if (anomalyResponse == null) {
                throw new IllegalStateException(
                        "AI service returned an empty response"
                );
            }

            return anomalyResponse;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to call AI anomaly service: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}