package com.civicai.dto;

import java.time.LocalDateTime;

public class AgentToolLogDto {
    private Long id;
    private String toolName;
    private String inputSummary;
    private String outputSummary;
    private String executionStatus;
    private LocalDateTime createdAt;

    public AgentToolLogDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
