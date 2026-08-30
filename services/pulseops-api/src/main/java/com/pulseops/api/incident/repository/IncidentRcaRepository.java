package com.pulseops.api.incident.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.api.incident.entity.IncidentRca;

public interface IncidentRcaRepository
        extends JpaRepository<IncidentRca, Long> {

    Optional<IncidentRca> findByIncidentId(Long incidentId);
}