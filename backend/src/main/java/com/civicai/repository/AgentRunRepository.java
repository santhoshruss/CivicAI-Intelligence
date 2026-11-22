package com.civicai.repository;

import com.civicai.entity.AgentRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRun, Long> {
    Optional<AgentRun> findTopByIncidentIdOrderByStartedAtDesc(Long incidentId);
    List<AgentRun> findByIncidentIdOrderByStartedAtDesc(Long incidentId);
}
