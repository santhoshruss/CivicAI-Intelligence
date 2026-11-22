package com.civicai.repository;

import com.civicai.entity.DuplicateIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuplicateIncidentRepository extends JpaRepository<DuplicateIncident, Long> {
    List<DuplicateIncident> findByIncidentId(Long incidentId);
    boolean existsByIncidentIdAndRelatedIncidentId(Long incidentId, Long relatedIncidentId);
}
