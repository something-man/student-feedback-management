package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.RequestStatus;
import java.util.UUID;

public class RequestUpdateRequest {
    private RequestStatus status;
    private UUID assignedToUserId;
    private String assignedCell;
    private String adminNote;

    public RequestUpdateRequest() {
    }

    public RequestUpdateRequest(RequestStatus status, String adminNote) {
        this.status = status;
        this.adminNote = adminNote;
    }

    public RequestUpdateRequest(RequestStatus status, UUID assignedToUserId, String assignedCell, String adminNote) {
        this.status = status;
        this.assignedToUserId = assignedToUserId;
        this.assignedCell = assignedCell;
        this.adminNote = adminNote;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
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
}

