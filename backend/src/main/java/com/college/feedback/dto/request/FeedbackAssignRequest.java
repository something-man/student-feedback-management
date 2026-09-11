package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.TargetAudience;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FeedbackAssignRequest {

    private List<UUID> userIds = new ArrayList<>();
    private String targetDepartment;
    private TargetAudience targetAudience;
    private LocalDateTime deadline;

    public FeedbackAssignRequest() {
    }

    public FeedbackAssignRequest(List<UUID> userIds, LocalDateTime deadline) {
        this.userIds = userIds;
        this.deadline = deadline;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }

    public String getTargetDepartment() {
        return targetDepartment;
    }

    public void setTargetDepartment(String targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience targetAudience) {
        this.targetAudience = targetAudience;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
