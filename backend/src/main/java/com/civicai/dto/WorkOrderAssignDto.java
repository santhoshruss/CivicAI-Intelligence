package com.civicai.dto;

import jakarta.validation.constraints.NotNull;

public class WorkOrderAssignDto {
    @NotNull(message = "Inspector ID is required")
    private Long inspectorId;

    public WorkOrderAssignDto() {}

    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
}
