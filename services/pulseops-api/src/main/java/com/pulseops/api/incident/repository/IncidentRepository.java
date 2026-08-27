package com.pulseops.api.incident.repository;

import com.pulseops.api.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByIncidentKey(String incidentKey);

    boolean existsByIncidentKey(String incidentKey);
}