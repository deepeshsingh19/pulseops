CREATE TABLE incidents (
    id BIGSERIAL PRIMARY KEY,

    incident_key VARCHAR(50) NOT NULL UNIQUE,

    title VARCHAR(200) NOT NULL,

    description VARCHAR(2000),

    severity VARCHAR(20) NOT NULL,

    status VARCHAR(20) NOT NULL,

    service_name VARCHAR(100) NOT NULL,

    detected_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_incidents_status
    ON incidents(status);

CREATE INDEX idx_incidents_severity
    ON incidents(severity);

CREATE INDEX idx_incidents_service_name
    ON incidents(service_name);

CREATE INDEX idx_incidents_created_at
    ON incidents(created_at DESC);