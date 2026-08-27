CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,

    aggregate_type VARCHAR(100) NOT NULL,

    aggregate_id VARCHAR(100) NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    topic VARCHAR(200) NOT NULL,

    payload JSONB NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMPTZ NOT NULL,

    published_at TIMESTAMPTZ
);

CREATE INDEX idx_outbox_events_status_created
    ON outbox_events(status, created_at);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events(aggregate_type, aggregate_id);