package com.pulseops.processor.kafka.consumer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.pulseops.common.events.IncidentRcaCompletedEvent;
import com.pulseops.common.events.RcaRequestedEvent;
import com.pulseops.processor.ai.RcaServiceClient;
import com.pulseops.processor.evidence.IncidentEvidence;
import com.pulseops.processor.evidence.IncidentEvidenceCollector;
import com.pulseops.processor.kafka.producer.IncidentRcaCompletedProducer;

@Component
public class RcaRequestedConsumer {

    private final IncidentEvidenceCollector evidenceCollector;
    private final RcaServiceClient rcaServiceClient;
    private final IncidentRcaCompletedProducer rcaCompletedProducer;

    public RcaRequestedConsumer(
            IncidentEvidenceCollector evidenceCollector,
            RcaServiceClient rcaServiceClient,
            IncidentRcaCompletedProducer rcaCompletedProducer) {

        this.evidenceCollector = evidenceCollector;
        this.rcaServiceClient = rcaServiceClient;
        this.rcaCompletedProducer = rcaCompletedProducer;
    }

    @KafkaListener(
            topics = "incidents.rca.requested",
            groupId = "pulseops-rca-processor",
            containerFactory = "rcaRequestedKafkaListenerContainerFactory"
    )
    public void processRcaRequest(
            RcaRequestedEvent request) {

        IncidentEvidence evidence =
                evidenceCollector.collect(
                        request.incidentKey(),
                        request.serviceName(),
                        request.title(),
                        request.severity()
                );

        String rcaJson =
                rcaServiceClient.analyze(evidence);

        IncidentRcaCompletedEvent completedEvent =
                parseRcaResponse(
                        request,
                        rcaJson
                );

        rcaCompletedProducer.publish(
                completedEvent
        );
    }

    private IncidentRcaCompletedEvent parseRcaResponse(
            RcaRequestedEvent request,
            String responseJson) {

        try {

            var mapper =
                    new tools.jackson.databind.json.JsonMapper();

            Map<?, ?> response =
                    mapper.readValue(
                            responseJson,
                            Map.class
                    );

            String rootCause =
                    String.valueOf(
                            response.get("root_cause")
                    );

            double confidence =
                    ((Number)
                            response.get("confidence"))
                            .doubleValue();

            String impact =
                    String.valueOf(
                            response.get("impact")
                    );

            List<String> evidence =
                    mapper.convertValue(
                            response.get("evidence"),
                            List.class
                    );

            List<String> actions =
                    mapper.convertValue(
                            response.get("recommended_actions"),
                            List.class
                    );

            return new IncidentRcaCompletedEvent(
                    request.incidentId(),
                    request.incidentKey(),
                    rootCause,
                    confidence,
                    impact,
                    evidence,
                    actions,
                    Instant.now()
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to parse RCA response",
                    exception
            );
        }
    }
}