package com.civicai.dto;

import java.time.LocalDateTime;

public class DuplicateResponseDto {
    private Long id;
    private Long incidentId;
    private Long relatedIncidentId;
    private String relatedReferenceNumber;
    private String relatedCategory;
    private String relatedAddress;
    private Double distanceMeters;
    private Double similarityScore;
    private String similarityLevel;
    private String explanation;
    private String reviewStatus;
    private LocalDateTime createdAt;

    public DuplicateResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

    public Long getRelatedIncidentId() { return relatedIncidentId; }
    public void setRelatedIncidentId(Long relatedIncidentId) { this.relatedIncidentId = relatedIncidentId; }

    public String getRelatedReferenceNumber() { return relatedReferenceNumber; }
    public void setRelatedReferenceNumber(String relatedReferenceNumber) { this.relatedReferenceNumber = relatedReferenceNumber; }

    public String getRelatedCategory() { return relatedCategory; }
    public void setRelatedCategory(String relatedCategory) { this.relatedCategory = relatedCategory; }

    public String getRelatedAddress() { return relatedAddress; }
    public void setRelatedAddress(String relatedAddress) { this.relatedAddress = relatedAddress; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

    public String getSimilarityLevel() { return similarityLevel; }
    public void setSimilarityLevel(String similarityLevel) { this.similarityLevel = similarityLevel; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
