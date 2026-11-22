package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateDto {
    @NotBlank(message = "Status is required")
    private String status;
    private String priority;
    private String explanation;

    public StatusUpdateDto() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
