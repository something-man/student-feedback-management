package com.college.feedback.dto.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacultyDashboardDto {
    private long pendingFeedbackCount;
    private double averageRating;
    private double studentResponseRate;
    private double syllabusMilestones;
    private Map<String, Double> ratingBreakdown = new HashMap<>();
    private List<FeedbackFormDto> assignedFeedbacks = new ArrayList<>();
    private List<AIInsightDto> aiInsights = new ArrayList<>();
    private List<NotificationDto> notifications = new ArrayList<>();

    public FacultyDashboardDto() {
    }

    public long getPendingFeedbackCount() {
        return pendingFeedbackCount;
    }

    public void setPendingFeedbackCount(long pendingFeedbackCount) {
        this.pendingFeedbackCount = pendingFeedbackCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public double getStudentResponseRate() {
        return studentResponseRate;
    }

    public void setStudentResponseRate(double studentResponseRate) {
        this.studentResponseRate = studentResponseRate;
    }

    public double getSyllabusMilestones() {
        return syllabusMilestones;
    }

    public void setSyllabusMilestones(double syllabusMilestones) {
        this.syllabusMilestones = syllabusMilestones;
    }

    public Map<String, Double> getRatingBreakdown() {
        return ratingBreakdown;
    }

    public void setRatingBreakdown(Map<String, Double> ratingBreakdown) {
        this.ratingBreakdown = ratingBreakdown;
    }

    public List<FeedbackFormDto> getAssignedFeedbacks() {
        return assignedFeedbacks;
    }

    public void setAssignedFeedbacks(List<FeedbackFormDto> assignedFeedbacks) {
        this.assignedFeedbacks = assignedFeedbacks;
    }

    public List<AIInsightDto> getAiInsights() {
        return aiInsights;
    }

    public void setAiInsights(List<AIInsightDto> aiInsights) {
        this.aiInsights = aiInsights;
    }

    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationDto> notifications) {
        this.notifications = notifications;
    }
}
