package com.college.feedback.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeedbackAnalyticsDto {

    private UUID formId;
    private String formTitle;
    private String category;
    private Long totalResponses = 0L;
    private Long totalAssignments = 0L;
    private Long completedAssignments = 0L;
    private Long pendingAssignments = 0L;
    private Double responseRate = 0.0;
    private Double averageRating = 0.0;
    private Map<Integer, Long> ratingDistribution = new HashMap<>();
    private Map<String, Double> categoryRatings = new HashMap<>();
    private Map<String, Long> categoryResponses = new HashMap<>();
    private List<Map<String, Object>> questionStats = new ArrayList<>();

    public FeedbackAnalyticsDto() {
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
    }

    public UUID getFormId() {
        return formId;
    }

    public void setFormId(UUID formId) {
        this.formId = formId;
    }

    public String getFormTitle() {
        return formTitle;
    }

    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(Long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public Long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Long getCompletedAssignments() {
        return completedAssignments;
    }

    public void setCompletedAssignments(Long completedAssignments) {
        this.completedAssignments = completedAssignments;
    }

    public Long getPendingAssignments() {
        return pendingAssignments;
    }

    public void setPendingAssignments(Long pendingAssignments) {
        this.pendingAssignments = pendingAssignments;
    }

    public Double getResponseRate() {
        return responseRate;
    }

    public void setResponseRate(Double responseRate) {
        this.responseRate = responseRate;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public Map<String, Double> getCategoryRatings() {
        return categoryRatings;
    }

    public void setCategoryRatings(Map<String, Double> categoryRatings) {
        this.categoryRatings = categoryRatings;
    }

    public Map<String, Long> getCategoryResponses() {
        return categoryResponses;
    }

    public void setCategoryResponses(Map<String, Long> categoryResponses) {
        this.categoryResponses = categoryResponses;
    }

    public Double getAvgRating() {
        return averageRating;
    }

    public void setAvgRating(Double avgRating) {
        this.averageRating = avgRating;
    }

    public Long getResponseCount() {
        return totalResponses;
    }

    public void setResponseCount(Long responseCount) {
        this.totalResponses = responseCount;
    }

    public List<Map<String, Object>> getQuestionBreakdown() {
        return questionStats;
    }

    public void setQuestionBreakdown(List<Map<String, Object>> questionBreakdown) {
        this.questionStats = questionBreakdown;
    }

    public List<Map<String, Object>> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(List<Map<String, Object>> questionStats) {
        this.questionStats = questionStats;
    }
}
