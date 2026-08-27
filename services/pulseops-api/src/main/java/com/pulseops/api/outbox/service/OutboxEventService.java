package com.pulseops.api.outbox.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.api.outbox.entity.OutboxEvent;
import com.pulseops.api.outbox.repository.OutboxEventRepository;

/**
 * Handles transactional state changes for outbox events.
 */
@Service
public class OutboxEventService {

    private final OutboxEventRepository repository;

    public OutboxEventService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    /**
     * Marks an outbox event as published in its own transaction.
     *
     * We keep this in a separate Spring bean because calling a
     * @Transactional method from the same class bypasses Spring's
     * transaction proxy.
     */
    @Transactional
    public void markPublished(OutboxEvent event) {

        event.markPublished();

        repository.save(event);
    }
}