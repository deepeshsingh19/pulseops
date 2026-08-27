package com.pulseops.api.telemetry.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulseops.api.telemetry.dto.CreateTelemetryEventRequest;
import com.pulseops.api.telemetry.dto.TelemetryEventResponse;
import com.pulseops.api.telemetry.service.TelemetryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(
            TelemetryService telemetryService) {

        this.telemetryService = telemetryService;
    }

    @PostMapping("/events")
    public ResponseEntity<TelemetryEventResponse> ingest(
            @Valid @RequestBody CreateTelemetryEventRequest request) {

        TelemetryEventResponse response =
                telemetryService.ingest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}