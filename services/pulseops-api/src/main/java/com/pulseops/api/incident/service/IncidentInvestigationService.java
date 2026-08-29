package com.pulseops.api.incident.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulseops.api.incident.entity.Incident;
import com.pulseops.api.incident.repository.IncidentRepository;
import com.pulseops.common.events.IncidentDetectedEvent;

@Service
public class IncidentInvestigationService {

    private final IncidentRepository incidentRepository;

    public IncidentInvestigationService(
            IncidentRepository incidentRepository) {

        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public Incident createIncident(
            IncidentDetectedEvent event) {

        Incident incident = new Incident();

        incident.setIncidentKey(event.incidentKey());
        incident.setTitle(event.title());
        incident.setDescription(event.description());
        incident.setSeverity(
                com.pulseops.api.incident.entity.IncidentSeverity
                        .valueOf(event.severity())
        );
        incident.setStatus(
                com.pulseops.api.incident.entity.IncidentStatus.OPEN
        );
        incident.setServiceName(event.serviceName());

        return incidentRepository.save(incident);
    }
}