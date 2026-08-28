package com.pulseops.processor.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
             * Keep the request explicit:
             * - HTTP/1.1
             * - JSON content type
             * - JSON body
             *
             * This avoids framework-specific message conversion.
             */
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl + "/api/v1/anomaly/score"
                    ))
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header(
                            "Content-Length",
                            String.valueOf(
                                    jsonBody.getBytes(
                                            java.nio.charset.StandardCharsets.UTF_8
                                    ).length
                            )
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    jsonBody
                            )
                    )
                    .build();

            System.out.println(
                    "Calling AI service with body: " + jsonBody
            );

            HttpResponse<String> response =
                    httpClient.send(
                            httpRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "AI service response: HTTP "
                            + response.statusCode()
                            + " "
                            + response.body()
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