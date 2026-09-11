package com.college.feedback.entity;

import com.college.feedback.entity.enums.InsightType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_insights", indexes = {
        @Index(name = "idx_insight_type", columnList = "insightType"),
        @Index(name = "idx_insight_source", columnList = "sourceType, sourceId")
})
public class AIInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50)
    private String sourceType; // FEEDBACK_FORM, COMPLAINT, CAMPUS_WIDE

    private UUID sourceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsightType insightType = InsightType.SENTIMENT;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String content; // Also referred to as description

    @Column(length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    private Double confidence; // Also referred to as confidenceScore

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private FeedbackForm feedbackForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AIInsight() {
    }

    public AIInsight(InsightType insightType, String title, String content, Double confidence) {
        this.insightType = insightType;
        this.title = title;
        this.content = content;
        this.confidence = confidence;
        this.sourceType = "CAMPUS_WIDE";
    }

    public AIInsight(String sourceType, UUID sourceId, InsightType insightType, String title, String content, String priority, Double confidence) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.insightType = insightType;
        this.title = title;
        this.content = content;
        this.priority = priority;
        this.confidence = confidence;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.feedbackForm != null && this.sourceId == null) {
            this.sourceType = "FEEDBACK_FORM";
            this.sourceId = this.feedbackForm.getId();
        }
        if (this.complaint != null && this.sourceId == null) {
            this.sourceType = "COMPLAINT";
            this.sourceId = this.complaint.getId();
        }
    }

    // Compatibility Getters & Setters
    public String getDescription() {
        return content;
    }

    public void setDescription(String description) {
        this.content = description;
    }

    public Double getConfidenceScore() {
        return confidence;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidence = confidenceScore;
    }

    public LocalDateTime getGeneratedAt() {
        return createdAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.createdAt = generatedAt;
    }

    // Standard Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public FeedbackForm getFeedbackForm() {
        return feedbackForm;
    }

    public void setFeedbackForm(FeedbackForm feedbackForm) {
        this.feedbackForm = feedbackForm;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
