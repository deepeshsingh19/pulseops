package com.pulseops.processor.evidence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Small client for the Prometheus HTTP API.
 *
 * Keeping this as a separate component means the evidence collector
 * doesn't need to know how Prometheus queries are transported.
 */
@Component
public class PrometheusClient {

    private final HttpClient httpClient;
    private final String baseUrl;

    public PrometheusClient(
            @Value("${pulseops.prometheus.base-url}") String baseUrl) {

        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;
    }

    public String query(String promql) {

        try {
            String encodedQuery =
                    java.net.URLEncoder.encode(
                            promql,
                            java.nio.charset.StandardCharsets.UTF_8
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
                            .GET()
                            .header("Accept", "application/json")
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