/*
 * Outbox payloads are serialized JSON strings.
 *
 * We don't query individual JSON fields in PostgreSQL, so TEXT is
 * sufficient and avoids unnecessary JSONB/JPA type mapping complexity.
 */
ALTER TABLE outbox_events
ALTER COLUMN payload TYPE TEXT;