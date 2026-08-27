/*
 * Stores normalized telemetry received by PulseOps.
 *
 * Raw/high-volume telemetry can eventually be archived in S3.
 * PostgreSQL stores the structured representation needed for
 * searching, correlation and incident analysis.
 */
CREATE TABLE telemetry_events (
    id BIGSERIAL PRIMARY KEY,

    event_id VARCHAR(100) NOT NULL UNIQUE,

    service_name VARCHAR(100) NOT NULL,

    event_type VARCHAR(30) NOT NULL,

    severity VARCHAR(20) NOT NULL,

    event_timestamp TIMESTAMPTZ NOT NULL,

    message TEXT,

    metadata TEXT,

    trace_id VARCHAR(100),

    span_id VARCHAR(100),

    source VARCHAR(100),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_telemetry_service_timestamp
    ON telemetry_events(service_name, event_timestamp DESC);

CREATE INDEX idx_telemetry_type_timestamp
    ON telemetry_events(event_type, event_timestamp DESC);

CREATE INDEX idx_telemetry_severity
    ON telemetry_events(severity);

CREATE INDEX idx_telemetry_trace_id
    ON telemetry_events(trace_id);