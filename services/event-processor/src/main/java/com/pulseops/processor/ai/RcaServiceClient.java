package com.pulseops.processor.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pulseops.processor.evidence.IncidentEvidence;

import tools.jackson.databind.json.JsonMapper;

@Component
public class RcaServiceClient {

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String baseUrl;

    public RcaServiceClient(
            JsonMapper jsonMapper,
            @Value("${pulseops.ai.base-url}") String baseUrl) {

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.jsonMapper = jsonMapper;
        this.baseUrl = baseUrl;
    }

    public String analyze(
            IncidentEvidence evidence) {

        try {

            Map<String, Object> requestBody =
                    new HashMap<>();

            requestBody.put(
                    "incident_key",
                    evidence.incidentKey()
            );

            requestBody.put(
                    "service_name",
                    evidence.serviceName()
            );

            requestBody.put(
                    "title",
                    evidence.title()
            );

            requestBody.put(
                    "severity",
                    evidence.severity()
            );

            requestBody.put(
                    "description",
                    "Incident detected by the PulseOps correlation engine."
            );

            requestBody.put(
                    "telemetry",
                    evidence.recentTelemetry()
            );

            requestBody.put(
                    "metrics",
                    evidence.metrics()
            );

            /*
             * Convert trace records into plain JSON-friendly maps.
             * This keeps the request independent of Java record internals.
             */
            List<Map<String, Object>> traces =
                    new ArrayList<>();

            for (var trace : evidence.traces()) {

                Map<String, Object> traceData =
                        new HashMap<>();

                traceData.put(
                        "traceId",
                        trace.traceId()
                );

                traceData.put(
                        "serviceName",
                        trace.serviceName()
                );

                traceData.put(
                        "operation",
                        trace.operation()
                );

                traceData.put(
                        "durationMs",
                        trace.duration().toMillis()
                );

                traceData.put(
                        "status",
                        trace.status()
                );

                traceData.put(
                        "errorMessage",
                        trace.errorMessage()
                );

                traces.add(traceData);
            }

            requestBody.put(
                    "traces",
                    traces
            );

            requestBody.put(
                    "evidence",
                    List.<String>of()
            );

            String jsonBody =
                    jsonMapper.writeValueAsString(
                            requestBody
                    );

            System.out.println(
                    "Sending RCA request: "
                            + jsonBody
            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl
                                                    + "/api/v1/rca/analyze"
                                    )
                            )
                            .version(
                                    HttpClient.Version.HTTP_1_1
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .header(
                                    "Accept",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(jsonBody)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "RCA service returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to call RCA service: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}