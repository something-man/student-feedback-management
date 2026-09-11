package com.college.feedback.dto.response;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardDto {
    private long totalResponses;
    private double averageRating;
    private long activeComplaints;
    private long pendingRequests;
    private long highPriorityIssues;
    private long escalatedIssues;
    private List<ComplaintDto> recentComplaints = new ArrayList<>();
    private List<RequestDto> recentRequests = new ArrayList<>();
    private List<AIInsightDto> aiAlerts = new ArrayList<>();
    private List<FeedbackFormDto> recentForms = new ArrayList<>();

    public AdminDashboardDto() {
    }

    public long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getActiveComplaints() {
        return activeComplaints;
    }

    public long getActiveComplaintsCount() {
        return activeComplaints;
    }

    public void setActiveComplaints(long activeComplaints) {
        this.activeComplaints = activeComplaints;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public long getPendingRequestsCount() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getHighPriorityIssues() {
        return highPriorityIssues;
    }

    public long getHighPriorityCount() {
        return highPriorityIssues;
    }

    public void setHighPriorityIssues(long highPriorityIssues) {
        this.highPriorityIssues = highPriorityIssues;
    }

    public long getEscalatedIssues() {
        return escalatedIssues;
    }

    public long getEscalatedCount() {
        return escalatedIssues;
    }

    public void setEscalatedIssues(long escalatedIssues) {
        this.escalatedIssues = escalatedIssues;
    }

    public List<ComplaintDto> getRecentComplaints() {
        return recentComplaints;
    }

    public void setRecentComplaints(List<ComplaintDto> recentComplaints) {
        this.recentComplaints = recentComplaints;
    }

    public List<RequestDto> getRecentRequests() {
        return recentRequests;
    }

    public void setRecentRequests(List<RequestDto> recentRequests) {
        this.recentRequests = recentRequests;
    }

    public List<AIInsightDto> getAiAlerts() {
        return aiAlerts;
    }

    public void setAiAlerts(List<AIInsightDto> aiAlerts) {
        this.aiAlerts = aiAlerts;
    }

    public List<FeedbackFormDto> getRecentForms() {
        return recentForms;
    }

    public void setRecentForms(List<FeedbackFormDto> recentForms) {
        this.recentForms = recentForms;
    }
}
