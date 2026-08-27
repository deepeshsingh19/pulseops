package com.pulseops.api.incident.controller;

import com.pulseops.api.incident.dto.CreateIncidentRequest;
import com.pulseops.api.incident.dto.IncidentResponse;
import com.pulseops.api.incident.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {

        IncidentResponse response =
                incidentService.createIncident(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}