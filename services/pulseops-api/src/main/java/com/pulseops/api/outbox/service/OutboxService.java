package com.pulseops.api.outbox.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.kafka.config.KafkaTopics;
import com.pulseops.api.outbox.entity.OutboxEvent;
import com.pulseops.api.outbox.repository.OutboxEventRepository;
import com.pulseops.common.events.IncidentCreatedEvent;

import tools.jackson.databind.json.JsonMapper;

/**
 * Creates durable outbox records.
 *
 * This service does NOT publish directly to Kafka.
 * The OutboxPublisher is responsible for reading PENDING records
 * and publishing them asynchronously.
 */
@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final JsonMapper jsonMapper;

    public OutboxService(
            OutboxEventRepository repository,
            JsonMapper jsonMapper) {

        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Creates the outbox event corresponding to a newly created incident.
     *
     * This method is called from IncidentService inside the same
     * database transaction as the incident insert.
     */
    public void createIncidentCreatedEvent(Incident incident) {

        IncidentCreatedEvent event = new IncidentCreatedEvent(
                incident.getId(),
                incident.getIncidentKey(),
                incident.getTitle(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getServiceName(),
                incident.getCreatedAt()
        );

        try {
            // Serialize the event into JSON before storing it in PostgreSQL.
            String payload = jsonMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "Incident",
                    incident.getId().toString(),
                    "IncidentCreated",
                    KafkaTopics.INCIDENTS_CREATED,
                    payload
            );

            repository.save(outboxEvent);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize IncidentCreatedEvent", e);
        }
    }
}