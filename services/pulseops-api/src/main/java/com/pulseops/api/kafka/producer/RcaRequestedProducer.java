package com.pulseops.api.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.pulseops.api.kafka.config.KafkaTopics;
import com.pulseops.common.events.RcaRequestedEvent;

@Component
public class RcaRequestedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RcaRequestedProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(
            RcaRequestedEvent event) {

        kafkaTemplate
                .send(
                        KafkaTopics.RCA_REQUESTED,
                        event.incidentKey(),
                        event
                )
                .whenComplete(
                        (result, exception) -> {

                            if (exception != null) {

                                System.err.println(
                                        "Failed to publish RCA request for "
                                                + event.incidentKey()
                                                + ": "
                                                + exception.getMessage()
                                );

                                return;
                            }

                            System.out.println(
                                    "Published RCA request for "
                                            + event.incidentKey()
                                            + " to "
                                            + result.getRecordMetadata()
                                                    .topic()
                            );
                        }
                );
    }
}