package com.pulseops.api.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}