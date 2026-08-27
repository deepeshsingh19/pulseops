package com.pulseops.api.kafka.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String INCIDENTS_CREATED = "incidents.created";

    public static final String TELEMETRY_EVENTS = "telemetry.events";

    public static final String INCIDENTS_DETECTED = "incidents.detected";
}