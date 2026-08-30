CREATE TABLE incident_rca (
    id BIGSERIAL PRIMARY KEY,

    incident_id BIGINT NOT NULL UNIQUE,

    root_cause TEXT NOT NULL,

    confidence DOUBLE PRECISION NOT NULL,

    impact TEXT NOT NULL,

    evidence TEXT NOT NULL,

    recommended_actions TEXT NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_incident_rca_incident
        FOREIGN KEY (incident_id)
        REFERENCES incidents(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_incident_rca_incident_id
    ON incident_rca(incident_id);

CREATE INDEX idx_incident_rca_created_at
    ON incident_rca(created_at DESC);