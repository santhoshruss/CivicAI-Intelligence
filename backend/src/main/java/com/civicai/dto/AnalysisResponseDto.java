package com.civicai.dto;

import java.time.LocalDateTime;

public class AnalysisResponseDto {
    private Long id;
    private Long incidentId;
    private String modelName;
    private String modelVersion;
    private Boolean isHeuristic;
    private String predictedCategory;
    private Double confidence;
    private String analysisStatus;
    private String explanation;
    private String detectedObjects;
    private LocalDateTime analyzedAt;

    public AnalysisResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }

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
