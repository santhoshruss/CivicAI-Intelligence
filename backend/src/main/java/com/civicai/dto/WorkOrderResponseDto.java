package com.civicai.dto;

import java.time.LocalDateTime;

public class WorkOrderResponseDto {
    private Long id;
    private Long incidentId;
    private String incidentRef;
    private String incidentCategory;
    private String incidentAddress;
    private Long inspectorId;
    private String inspectorName;
    private Long departmentId;
    private String departmentName;
    private String priority;
    private String description;
    private String status;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkOrderResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public String getIncidentRef() { return incidentRef; }
    public void setIncidentRef(String incidentRef) { this.incidentRef = incidentRef; }

    public String getIncidentCategory() { return incidentCategory; }
    public void setIncidentCategory(String incidentCategory) { this.incidentCategory = incidentCategory; }

    public String getIncidentAddress() { return incidentAddress; }
    public void setIncidentAddress(String incidentAddress) { this.incidentAddress = incidentAddress; }

    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApprovedById() { return approvedById; }
    public void setApprovedById(Long approvedById) { this.approvedById = approvedById; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
