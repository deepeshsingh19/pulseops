package com.pulseops.processor.evidence;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads recent trace information from Grafana Tempo.
 *
 * The RCA pipeline only needs a small amount of trace metadata,
 * not the complete Tempo response.
 */
@Component
public class TempoClient {

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String baseUrl;

    public TempoClient(
            JsonMapper jsonMapper,
            @Value("${pulseops.tempo.base-url}") String baseUrl) {

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.jsonMapper = jsonMapper;
        this.baseUrl = baseUrl;
    }

    public List<TraceEvidence> searchRecentTraces(
            String serviceName) {

        try {
            String tags =
                    "service.name=" + serviceName;

            String encodedTags =
                    URLEncoder.encode(
                            tags,
                            StandardCharsets.UTF_8
                    );

            String url =
                    baseUrl
                            + "/api/search?tags="
                            + encodedTags
                            + "&limit=5";

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .version(HttpClient.Version.HTTP_1_1)
                            .header("Accept", "application/json")
                            .GET()
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Tempo returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return parseTraceResponse(
                    response.body(),
                    serviceName
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to query Tempo",
                    exception
            );
        }
    }

    private List<TraceEvidence> parseTraceResponse(
            String responseBody,
            String serviceName) {

        try {
            JsonNode root =
                    jsonMapper.readTree(responseBody);

            JsonNode traces =
                    root.path("traces");

            List<TraceEvidence> result =
                    new ArrayList<>();

            if (!traces.isArray()) {
                return result;
            }

            for (JsonNode trace : traces) {

                String traceId =
                        trace.path("traceID")
                                .asText("");

                String rootService =
                        trace.path("rootServiceName")
                                .asText(serviceName);

                String rootTraceName =
                        trace.path("rootTraceName")
                                .asText("");

                long durationMs =
                        trace.path("durationMs")
                                .asLong(0);

                result.add(
                        new TraceEvidence(
                                traceId,
                                rootService,
                                rootTraceName,
                                java.time.Duration.ofMillis(
                                        durationMs
                                ),
                                "UNKNOWN",
                                ""
                        )
                );
            }

            return result;

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse Tempo response",
                    exception
            );
        }
    }
}