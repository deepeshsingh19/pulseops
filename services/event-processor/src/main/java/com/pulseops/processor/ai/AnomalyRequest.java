package com.pulseops.processor.ai;

import java.util.Map;

public record AnomalyRequest(
        String serviceName,
        Map<String, Object> metadata
) {
}