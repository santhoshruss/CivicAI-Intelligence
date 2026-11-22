package com.civicai.repository;

import com.civicai.entity.IncidentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentAnalysisRepository extends JpaRepository<IncidentAnalysis, Long> {
    Optional<IncidentAnalysis> findTopByIncidentIdOrderByAnalyzedAtDesc(Long incidentId);
    List<IncidentAnalysis> findByIncidentId(Long incidentId);
}
