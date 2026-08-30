package com.pulseops.api.incident.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulseops.api.incident.dto.CreateIncidentRequest;
import com.pulseops.api.incident.dto.IncidentDetailResponse;
import com.pulseops.api.incident.dto.IncidentResponse;
import com.pulseops.api.incident.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(
            IncidentService incidentService) {

        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestBody CreateIncidentRequest request) {

        IncidentResponse response =
                incidentService.createIncident(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentDetailResponse> getIncident(
            @PathVariable Long incidentId) {

        return ResponseEntity.ok(
                incidentService.getIncident(
                        incidentId
                )
        );
    }
}