package com.college.feedback.dto.response;

import com.college.feedback.entity.IssueUpdate;
import com.college.feedback.entity.enums.IssueType;
import java.time.LocalDateTime;
import java.util.UUID;

public class IssueUpdateDto {
    private UUID id;
    private IssueType issueType;
    private String updatedByName;
    private String updatedByRole;
    private String statusUpdate;
    private String comment;
    private LocalDateTime createdAt;

    public IssueUpdateDto() {
    }

    public static IssueUpdateDto fromEntity(IssueUpdate update) {
        if (update == null) return null;
        IssueUpdateDto dto = new IssueUpdateDto();
        dto.setId(update.getId());
        dto.setIssueType(update.getIssueType());
        if (update.getUpdatedBy() != null) {
            dto.setUpdatedByName(update.getUpdatedBy().getFullName());
            dto.setUpdatedByRole(update.getUpdatedBy().getRole().name());
        }
        dto.setStatusUpdate(update.getStatusUpdate());
        dto.setComment(update.getComment());
        dto.setCreatedAt(update.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public String getUpdatedByRole() {
        return updatedByRole;
    }

    public void setUpdatedByRole(String updatedByRole) {
        this.updatedByRole = updatedByRole;
    }

    public String getStatusUpdate() {
        return statusUpdate;
    }

    public void setStatusUpdate(String statusUpdate) {
        this.statusUpdate = statusUpdate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
