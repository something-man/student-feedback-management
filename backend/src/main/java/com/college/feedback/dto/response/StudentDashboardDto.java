package com.college.feedback.dto.response;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardDto {
    private long pendingFeedbackCount;
    private long activeComplaintsCount;
    private long pendingRequestsCount;
    private List<FeedbackFormDto> assignedFeedbacks = new ArrayList<>();
    private List<ComplaintDto> recentComplaints = new ArrayList<>();
    private List<RequestDto> recentRequests = new ArrayList<>();
    private List<NotificationDto> notifications = new ArrayList<>();

    public StudentDashboardDto() {
    }

    public long getPendingFeedbackCount() {
        return pendingFeedbackCount;
    }

    public void setPendingFeedbackCount(long pendingFeedbackCount) {
        this.pendingFeedbackCount = pendingFeedbackCount;
    }

    public long getActiveComplaintsCount() {
        return activeComplaintsCount;
    }

    public void setActiveComplaintsCount(long activeComplaintsCount) {
        this.activeComplaintsCount = activeComplaintsCount;
    }

    public long getPendingRequestsCount() {
        return pendingRequestsCount;
    }

    public void setPendingRequestsCount(long pendingRequestsCount) {
        this.pendingRequestsCount = pendingRequestsCount;
    }

    public List<FeedbackFormDto> getAssignedFeedbacks() {
        return assignedFeedbacks;
    }

    public void setAssignedFeedbacks(List<FeedbackFormDto> assignedFeedbacks) {
        this.assignedFeedbacks = assignedFeedbacks;
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

    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationDto> notifications) {
        this.notifications = notifications;
    }
}
