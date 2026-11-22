package com.civicai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_order_updates")
public class WorkOrderUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(name = "previous_status", length = 40)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 40)
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "evidence_filename", length = 255)
    private String evidenceFilename;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public WorkOrderUpdate() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getEvidenceFilename() { return evidenceFilename; }
    public void setEvidenceFilename(String evidenceFilename) { this.evidenceFilename = evidenceFilename; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
