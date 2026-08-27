package com.pulseops.api.outbox.publisher;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pulseops.api.outbox.entity.OutboxEvent;
import com.pulseops.api.outbox.entity.OutboxEventStatus;
import com.pulseops.api.outbox.repository.OutboxEventRepository;
import com.pulseops.api.outbox.service.OutboxEventService;

/**
 * Periodically reads pending events from the outbox table
 * and publishes them to Kafka.
 *
 * Flow:
 *
 * PostgreSQL
 *     ↓
 * outbox_events (PENDING)
 *     ↓
 * OutboxPublisher
 *     ↓
 * Kafka
 *     ↓
 * OutboxEventService
 *     ↓
 * PUBLISHED
 *
 * The publisher is responsible ONLY for sending messages.
 * The transactional database update is delegated to
 * OutboxEventService.
 */
@Component
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventService outboxEventService;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxEventService outboxEventService) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxEventService = outboxEventService;
    }

    /**
     * Runs every second and attempts to publish pending events.
     *
     * fixedDelay means Spring waits 1 second AFTER the previous
     * execution finishes before starting the next execution.
     */
    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxEventStatus.PENDING
                        );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        /*
         * The payload is already stored as JSON in the outbox table.
         *
         * Therefore Kafka receives a String and does not need to
         * serialize the event object again.
         */
        kafkaTemplate
                .send(
                        event.getTopic(),
                        event.getAggregateId(),
                        event.getPayload()
                )
                .whenComplete((result, exception) -> {

                    if (exception != null) {

                        /*
                         * Kafka failed.
                         *
                         * We intentionally DO NOT change the status.
                         * The event remains PENDING and will be retried
                         * during the next scheduled execution.
                         */
                        System.err.println(
                                "Failed to publish outbox event "
                                        + event.getId()
                                        + ": "
                                        + exception.getMessage()
                        );

                        return;
                    }

                    /*
                     * Kafka acknowledged the message successfully.
                     *
                     * Now we can mark the outbox event as PUBLISHED.
                     *
                     * This happens through a separate Spring service
                     * so that @Transactional is actually applied.
                     */
                    outboxEventService.markPublished(event);
                });
    }
}