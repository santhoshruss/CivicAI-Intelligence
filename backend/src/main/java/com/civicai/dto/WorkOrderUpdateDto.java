package com.civicai.dto;

import jakarta.validation.constraints.NotBlank;

public class WorkOrderUpdateDto {
    @NotBlank(message = "New status is required")
    private String newStatus;
    private String notes;
    private String evidenceFilename;

    public WorkOrderUpdateDto() {}

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEvidenceFilename() { return evidenceFilename; }
    public void setEvidenceFilename(String evidenceFilename) { this.evidenceFilename = evidenceFilename; }
}
