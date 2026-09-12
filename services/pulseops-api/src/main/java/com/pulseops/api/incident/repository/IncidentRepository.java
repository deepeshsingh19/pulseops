package com.pulseops.api.incident.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulseops.api.incident.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByCreatedAtDesc();

    Optional<Incident> findByIncidentKey(String incidentKey);

    boolean existsByIncidentKey(String incidentKey);
}