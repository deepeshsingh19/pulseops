package com.pulseops.api.telemetry.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.api.telemetry.entity.TelemetryEventEntity;

public interface TelemetryEventRepository
        extends JpaRepository<TelemetryEventEntity, Long> {

    Optional<TelemetryEventEntity> findByEventId(String eventId);

    boolean existsByEventId(String eventId);
}