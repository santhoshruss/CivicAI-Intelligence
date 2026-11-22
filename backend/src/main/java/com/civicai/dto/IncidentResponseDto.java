package com.civicai.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class IncidentResponseDto {
    private Long id;
    private String referenceNumber;
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;
    private String category;
    private String description;
    private String originalLanguage;
    private String normalizedDescription;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String status;
    private String recommendedPriority;
    private String approvedPriority;
    private Long assignedDepartmentId;
    private String assignedDepartmentName;
    private List<String> evidenceFiles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public IncidentResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }

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

    public Long getAssignedDepartmentId() { return assignedDepartmentId; }
    public void setAssignedDepartmentId(Long assignedDepartmentId) { this.assignedDepartmentId = assignedDepartmentId; }

    public String getAssignedDepartmentName() { return assignedDepartmentName; }
    public void setAssignedDepartmentName(String assignedDepartmentName) { this.assignedDepartmentName = assignedDepartmentName; }

    public List<String> getEvidenceFiles() { return evidenceFiles; }
    public void setEvidenceFiles(List<String> evidenceFiles) { this.evidenceFiles = evidenceFiles; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
