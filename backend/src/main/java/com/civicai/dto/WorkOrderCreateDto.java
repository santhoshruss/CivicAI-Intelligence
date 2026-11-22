package com.civicai.dto;

import jakarta.validation.constraints.NotNull;

public class WorkOrderCreateDto {
    @NotNull(message = "Incident ID is required")
    private Long incidentId;

    private Long assignedInspectorId;
    private Long departmentId;
    private String priority;
    private String description;

    public WorkOrderCreateDto() {}

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public Long getAssignedInspectorId() { return assignedInspectorId; }
    public void setAssignedInspectorId(Long assignedInspectorId) { this.assignedInspectorId = assignedInspectorId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
