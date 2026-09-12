package com.pulseops.api.telemetry.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.api.telemetry.entity.TelemetryEventEntity;

public interface TelemetryEventRepository
        extends JpaRepository<TelemetryEventEntity, Long> {

    Optional<TelemetryEventEntity> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    List<TelemetryEventEntity>
    findByServiceNameAndEventTimestampBetweenOrderByEventTimestampAsc(
            String serviceName,
            Instant start,
            Instant end
    );
}