package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import java.util.UUID;

public class ComplaintUpdateRequest {
    private IssueStatus status;
    private UUID assignedToUserId;
    private String assignedCell;
    private String adminNote;
    private Boolean publicVisible;
    private Priority priority;

    public ComplaintUpdateRequest() {
    }

    public ComplaintUpdateRequest(IssueStatus status, String assignedCell, String adminNote) {
        this.status = status;
        this.assignedCell = assignedCell;
        this.adminNote = adminNote;
    }

    public ComplaintUpdateRequest(IssueStatus status, UUID assignedToUserId, String assignedCell, String adminNote, Boolean publicVisible, Priority priority) {
        this.status = status;
        this.assignedToUserId = assignedToUserId;
        this.assignedCell = assignedCell;
        this.adminNote = adminNote;
        this.publicVisible = publicVisible;
        this.priority = priority;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public UUID getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(UUID assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getAssignedCell() {
        return assignedCell;
    }

    public void setAssignedCell(String assignedCell) {
        this.assignedCell = assignedCell;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public Boolean getPublicVisible() {
        return publicVisible;
    }

    public void setPublicVisible(Boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}

