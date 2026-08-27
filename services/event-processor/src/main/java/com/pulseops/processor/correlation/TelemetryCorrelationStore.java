package com.pulseops.processor.correlation;

import com.pulseops.common.events.TelemetryEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the recent telemetry window used by the correlation engine.
 */
@Component
public class TelemetryCorrelationStore {

    private static final Duration EVENT_TTL = Duration.ofMinutes(2);

    private final RedisTemplate<String, String> redisTemplate;
    private final JsonMapper jsonMapper;

    public TelemetryCorrelationStore(
            RedisTemplate<String, String> redisTemplate,
            JsonMapper jsonMapper) {

        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public void add(String serviceName, TelemetryEvent event) {

        try {
            String key = buildKey(serviceName);

            String payload =
                    jsonMapper.writeValueAsString(event);

            redisTemplate.opsForList().rightPush(key, payload);

            // Keep old correlation windows from accumulating indefinitely.
            redisTemplate.expire(key, EVENT_TTL);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to store telemetry event in Redis",
                    e
            );
        }
    }

    public List<TelemetryEvent> getRecent(String serviceName) {

        String key = buildKey(serviceName);

        List<String> values =
                redisTemplate.opsForList().range(key, 0, -1);

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<TelemetryEvent> events = new ArrayList<>();

        for (String value : values) {
            try {
                events.add(
                        jsonMapper.readValue(
                                value,
                                TelemetryEvent.class
                        )
                );
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to deserialize telemetry event",
                        e
                );
            }
        }

        return events;
    }

    private String buildKey(String serviceName) {
        return "pulseops:correlation:" + serviceName;
    }
}