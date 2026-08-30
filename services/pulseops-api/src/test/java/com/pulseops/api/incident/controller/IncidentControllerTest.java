package com.pulseops.api.incident.controller;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.pulseops.api.incident.dto.CreateIncidentRequest;
import com.pulseops.api.incident.dto.IncidentDetailResponse;
import com.pulseops.api.incident.dto.IncidentResponse;
import com.pulseops.api.incident.entity.IncidentSeverity;
import com.pulseops.api.incident.entity.IncidentStatus;
import com.pulseops.api.incident.service.IncidentService;

@ExtendWith(MockitoExtension.class)
class IncidentControllerTest {

    @Mock
    private IncidentService incidentService;

    private IncidentController incidentController;

    @BeforeEach
    void setUp() {

        incidentController =
                new IncidentController(
                        incidentService
                );
    }

    @Test
    void shouldCreateIncident() {

        CreateIncidentRequest request =
                new CreateIncidentRequest(
                        "Payment failure",
                        "Payment requests are failing.",
                        IncidentSeverity.CRITICAL,
                        "payment-service"
                );

        Instant now = Instant.now();

        IncidentResponse serviceResponse =
                new IncidentResponse(
                        100L,
                        "INC-TEST1234",
                        "Payment failure",
                        "Payment requests are failing.",
                        IncidentSeverity.CRITICAL,
                        IncidentStatus.OPEN,
                        "payment-service",
                        now,
                        now,
                        now
                );

        when(
                incidentService.createIncident(
                        request
                )
        ).thenReturn(serviceResponse);

        ResponseEntity<IncidentResponse> response =
                incidentController.createIncident(
                        request
                );

        assertEquals(
                201,
                response.getStatusCode().value()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                100L,
                response.getBody().id()
        );

        assertEquals(
                "INC-TEST1234",
                response.getBody().incidentKey()
        );

        assertEquals(
                IncidentSeverity.CRITICAL,
                response.getBody().severity()
        );

        assertEquals(
                IncidentStatus.OPEN,
                response.getBody().status()
        );

        verify(
                incidentService
        ).createIncident(request);
    }

    @Test
    void shouldReturnIncidentWithRca() {

        IncidentDetailResponse.RcaResponse rca =
                new IncidentDetailResponse.RcaResponse(
                        "Database connection saturation.",
                        0.91,
                        "Payment requests are failing.",
                        List.of(
                                "Database latency reached 1431 ms.",
                                "Database pool usage reached 99.7%.",
                                "HTTP 5xx rate reached 12.8%."
                        ),
                        List.of(
                                "Inspect slow database queries.",
                                "Review database connection pool size."
                        )
                );

        Instant now = Instant.now();

        IncidentDetailResponse serviceResponse =
                new IncidentDetailResponse(
                        246L,
                        "AUTO-PAYMENT-SERVICE-10FC1503",
                        "Payment service degradation detected",
                        "Telemetry correlation detected abnormal payment-service behavior.",
                        "CRITICAL",
                        "OPEN",
                        "payment-service",
                        now,
                        now,
                        now,
                        rca
                );

        when(
                incidentService.getIncident(246L)
        ).thenReturn(serviceResponse);

        ResponseEntity<IncidentDetailResponse> response =
                incidentController.getIncident(246L);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                246L,
                response.getBody().id()
        );

        assertEquals(
                "AUTO-PAYMENT-SERVICE-10FC1503",
                response.getBody().incidentKey()
        );

        assertNotNull(
                response.getBody().rca()
        );

        assertEquals(
                "Database connection saturation.",
                response.getBody().rca().rootCause()
        );

        assertEquals(
                0.91,
                response.getBody().rca().confidence()
        );

        assertEquals(
                3,
                response.getBody().rca().evidence().size()
        );

        assertEquals(
                2,
                response.getBody().rca().recommendedActions().size()
        );

        verify(
                incidentService
        ).getIncident(246L);
    }
}