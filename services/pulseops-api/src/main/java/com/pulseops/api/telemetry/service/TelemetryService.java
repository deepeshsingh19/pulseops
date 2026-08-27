package com.pulseops.api.telemetry.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.api.kafka.config.KafkaTopics;
import com.pulseops.api.outbox.entity.OutboxEvent;
import com.pulseops.api.outbox.repository.OutboxEventRepository;
import com.pulseops.api.telemetry.dto.CreateTelemetryEventRequest;
import com.pulseops.api.telemetry.dto.TelemetryEventResponse;
import com.pulseops.api.telemetry.entity.TelemetryEventEntity;
import com.pulseops.api.telemetry.repository.TelemetryEventRepository;
import com.pulseops.common.events.TelemetryEvent;

import tools.jackson.databind.json.JsonMapper;

@Service
public class TelemetryService {

    private final TelemetryEventRepository telemetryRepository;
    private final OutboxEventRepository outboxRepository;
    private final JsonMapper jsonMapper;

    public TelemetryService(
            TelemetryEventRepository telemetryRepository,
            OutboxEventRepository outboxRepository,
            JsonMapper jsonMapper) {

        this.telemetryRepository = telemetryRepository;
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public TelemetryEventResponse ingest(
            CreateTelemetryEventRequest request) {

        /*
         * Prevent duplicate telemetry events.
         *
         * The same event may be delivered more than once
         * by an external producer.
         */
        if (telemetryRepository.existsByEventId(request.eventId())) {
            return telemetryRepository
                    .findByEventId(request.eventId())
                    .map(TelemetryEventResponse::from)
                    .orElseThrow();
        }

        String metadataJson = serializeMetadata(request.metadata());

        TelemetryEventEntity entity =
                new TelemetryEventEntity(
                        request.eventId(),
                        request.serviceName(),
                        request.eventType(),
                        request.severity(),
                        request.timestamp(),
                        request.message(),
                        metadataJson,
                        request.traceId(),
                        request.spanId(),
                        request.source()
                );

        TelemetryEventEntity saved =
                telemetryRepository.save(entity);

        /*
         * The shared object is the Kafka contract.
         * The database entity remains an internal persistence model.
         */
        TelemetryEvent event = new TelemetryEvent(
                request.eventId(),
                request.serviceName(),
                TelemetryEvent.EventType.valueOf(
                        request.eventType()
                ),
                request.severity(),
                request.timestamp(),
                request.message(),
                request.metadata(),
                request.traceId(),
                request.spanId()
        );

        try {

            String payload =
                    jsonMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "TelemetryEvent",
                    request.eventId(),
                    "TelemetryReceived",
                    KafkaTopics.TELEMETRY_EVENTS,
                    payload
            );

            outboxRepository.save(outboxEvent);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to create telemetry outbox event",
                    e
            );
        }

        return TelemetryEventResponse.from(saved);
    }

    private String serializeMetadata(
            java.util.Map<String, Object> metadata) {

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return jsonMapper.writeValueAsString(metadata);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid telemetry metadata", e);
        }
    }
}