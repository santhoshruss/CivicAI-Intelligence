package com.civicai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_analysis")
public class IncidentAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "is_heuristic")
    private Boolean isHeuristic = false;

    @Column(name = "predicted_category", nullable = false, length = 50)
    private String predictedCategory;

    @Column(nullable = false)
    private Double confidence;

    @Column(name = "analysis_status", nullable = false, length = 40)
    private String analysisStatus = "SUCCESS"; // SUCCESS, PENDING, FAILED, UNAVAILABLE

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "detected_objects", columnDefinition = "TEXT")
    private String detectedObjects;

    @Column(name = "analyzed_at", updatable = false)
    private LocalDateTime analyzedAt;

    public IncidentAnalysis() {}

    @PrePersist
    public void prePersist() {
        if (analyzedAt == null) {
            analyzedAt = LocalDateTime.now();
        }
        if (isHeuristic == null) {
            isHeuristic = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Boolean getIsHeuristic() { return isHeuristic; }
    public void setIsHeuristic(Boolean heuristic) { isHeuristic = heuristic; }

    public String getPredictedCategory() { return predictedCategory; }
    public void setPredictedCategory(String predictedCategory) { this.predictedCategory = predictedCategory; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(String analysisStatus) { this.analysisStatus = analysisStatus; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDetectedObjects() { return detectedObjects; }
    public void setDetectedObjects(String detectedObjects) { this.detectedObjects = detectedObjects; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
