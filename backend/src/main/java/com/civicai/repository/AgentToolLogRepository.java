package com.civicai.repository;

import com.civicai.entity.AgentToolLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentToolLogRepository extends JpaRepository<AgentToolLog, Long> {
    List<AgentToolLog> findByAgentRunIdOrderByCreatedAtAsc(Long agentRunId);
}
