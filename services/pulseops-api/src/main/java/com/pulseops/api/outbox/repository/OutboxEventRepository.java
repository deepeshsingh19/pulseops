package com.pulseops.api.outbox.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.api.outbox.entity.OutboxEvent;
import com.pulseops.api.outbox.entity.OutboxEventStatus;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
            OutboxEventStatus status
    );
}