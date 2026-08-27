package com.pulseops.api.incident.dto;

import com.pulseops.api.incident.entity.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        IncidentSeverity severity,

        @NotBlank
        @Size(max = 100)
        String serviceName
) {
}