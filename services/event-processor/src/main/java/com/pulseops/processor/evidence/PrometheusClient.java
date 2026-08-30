package com.pulseops.processor.evidence;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Small client for the Prometheus HTTP API.
 *
 * The client only handles HTTP transport. PromQL interpretation stays
 * inside the evidence collector.
 */
@Component
public class PrometheusClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public PrometheusClient(
            @Value("${pulseops.prometheus.base-url}") String baseUrl) {

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.baseUrl = baseUrl;
    }

    public String query(String promql) {

        try {
            String encodedQuery =
                    URLEncoder.encode(
                            promql,
                            StandardCharsets.UTF_8
                    );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(
                                            baseUrl
                                                    + "/api/v1/query?query="
                                                    + encodedQuery
                                    )
                            )
                            .version(HttpClient.Version.HTTP_1_1)
                            .header(
                                    "Accept",
                                    "application/json"
                            )
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
                        "Prometheus returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return response.body();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to query Prometheus",
                    exception
            );
        }
    }
}