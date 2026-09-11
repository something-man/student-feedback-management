package com.college.feedback.dto.response;

import com.college.feedback.entity.AIInsight;
import com.college.feedback.entity.enums.InsightType;
import java.time.LocalDateTime;
import java.util.UUID;

public class AIInsightDto {
    private UUID id;
    private InsightType insightType;
    private String title;
    private String description;
    private Double confidenceScore;
    private LocalDateTime generatedAt;

    public AIInsightDto() {
    }

    public static AIInsightDto fromEntity(AIInsight insight) {
        if (insight == null) return null;
        AIInsightDto dto = new AIInsightDto();
        dto.setId(insight.getId());
        dto.setInsightType(insight.getInsightType());
        dto.setTitle(insight.getTitle());
        dto.setDescription(insight.getDescription());
        dto.setConfidenceScore(insight.getConfidenceScore());
        dto.setGeneratedAt(insight.getGeneratedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InsightType getInsightType() {
        return insightType;
    }

    public void setInsightType(InsightType insightType) {
        this.insightType = insightType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
