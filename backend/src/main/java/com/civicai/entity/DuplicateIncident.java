package com.civicai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "duplicate_incidents")
public class DuplicateIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_incident_id", nullable = false)
    private Incident relatedIncident;

    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "similarity_level", length = 30)
    private String similarityLevel = "MEDIUM_SIMILARITY"; // HIGH_SIMILARITY, MEDIUM_SIMILARITY, LOW_SIMILARITY

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "review_status", length = 40)
    private String reviewStatus = "POSSIBLE_DUPLICATE"; // POSSIBLE_DUPLICATE, CONFIRMED_DUPLICATE, DISMISSED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DuplicateIncident() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (reviewStatus == null) {
            reviewStatus = "POSSIBLE_DUPLICATE";
        }
        if (similarityLevel == null) {
            similarityLevel = "MEDIUM_SIMILARITY";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public Incident getRelatedIncident() { return relatedIncident; }
    public void setRelatedIncident(Incident relatedIncident) { this.relatedIncident = relatedIncident; }

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
