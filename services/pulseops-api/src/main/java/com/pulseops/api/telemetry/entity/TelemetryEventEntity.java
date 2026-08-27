package com.pulseops.api.telemetry.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Database representation of a telemetry event.
 *
 *
 * Entity = database model
 * TelemetryEvent = event/message contract
 */
@Entity
@Table(name = "telemetry_events")
public class TelemetryEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "span_id", length = 100)
    private String spanId;

    @Column(length = 100)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TelemetryEventEntity() {
        // Required by JPA.
    }

    public TelemetryEventEntity(
            String eventId,
            String serviceName,
            String eventType,
            String severity,
            Instant eventTimestamp,
            String message,
            String metadata,
            String traceId,
            String spanId,
            String source) {

        this.eventId = eventId;
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.severity = severity;
        this.eventTimestamp = eventTimestamp;
        this.message = message;
        this.metadata = metadata;
        this.traceId = traceId;
        this.spanId = spanId;
        this.source = source;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}