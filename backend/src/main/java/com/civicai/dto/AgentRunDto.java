package com.civicai.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentRunDto {
    private Long id;
    private Long incidentId;
    private String executionStatus;
    private String recommendation;
    private List<AgentToolLogDto> toolLogs;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public AgentRunDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public List<AgentToolLogDto> getToolLogs() { return toolLogs; }
    public void setToolLogs(List<AgentToolLogDto> toolLogs) { this.toolLogs = toolLogs; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
