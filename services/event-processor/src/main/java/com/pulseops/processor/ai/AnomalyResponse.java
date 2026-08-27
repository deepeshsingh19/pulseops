package com.pulseops.processor.ai;

public record AnomalyResponse(
        String serviceName,
        double anomalyScore,
        boolean anomalous,
        boolean modelReady
) {
}