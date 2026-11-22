package com.civicai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(nullable = false, length = 50)
    private String category; // POTHOLE, ROAD_CRACK, STREETLIGHT_DAMAGE, GARBAGE_OVERFLOW, WATER_LEAKAGE

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_language", length = 20)
    private String originalLanguage = "English";

    @Column(name = "normalized_description", columnDefinition = "TEXT")
    private String normalizedDescription;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 40)
    private String status = "REPORTED"; // REPORTED, UNDER_REVIEW, AI_ANALYZED, WORK_ORDER_CREATED, IN_PROGRESS, RESOLVED, REJECTED

    @Column(name = "recommended_priority", length = 20)
    private String recommendedPriority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "approved_priority", length = 20)
    private String approvedPriority = "MEDIUM";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_department_id")
    private Department assignedDepartment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Incident() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "REPORTED";
        }
        if (recommendedPriority == null) {
            recommendedPriority = "MEDIUM";
        }
        if (approvedPriority == null) {
            approvedPriority = recommendedPriority;
        }
        if (originalLanguage == null) {
            originalLanguage = "English";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public User getCitizen() { return citizen; }
    public void setCitizen(User citizen) { this.citizen = citizen; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public String getNormalizedDescription() { return normalizedDescription; }
    public void setNormalizedDescription(String normalizedDescription) { this.normalizedDescription = normalizedDescription; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecommendedPriority() { return recommendedPriority; }
    public void setRecommendedPriority(String recommendedPriority) { this.recommendedPriority = recommendedPriority; }

    public String getApprovedPriority() { return approvedPriority; }
    public void setApprovedPriority(String approvedPriority) { this.approvedPriority = approvedPriority; }

    public Department getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(Department assignedDepartment) { this.assignedDepartment = assignedDepartment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
