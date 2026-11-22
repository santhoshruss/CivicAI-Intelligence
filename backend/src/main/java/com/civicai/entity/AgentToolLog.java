package com.civicai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_tool_logs")
public class AgentToolLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_run_id", nullable = false)
    private AgentRun agentRun;

    @Column(name = "tool_name", nullable = false, length = 100)
    private String toolName;

    @Column(name = "input_summary", columnDefinition = "TEXT")
    private String inputSummary;

    @Column(name = "output_summary", columnDefinition = "TEXT")
    private String outputSummary;

    @Column(name = "execution_status", length = 30)
    private String executionStatus = "SUCCESS";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AgentToolLog() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (executionStatus == null) {
            executionStatus = "SUCCESS";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AgentRun getAgentRun() { return agentRun; }
    public void setAgentRun(AgentRun agentRun) { this.agentRun = agentRun; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getInputSummary() { return inputSummary; }
    public void setInputSummary(String inputSummary) { this.inputSummary = inputSummary; }

    public String getOutputSummary() { return outputSummary; }
    public void setOutputSummary(String outputSummary) { this.outputSummary = outputSummary; }

    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
