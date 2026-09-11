package com.college.feedback.entity;

import com.college.feedback.entity.enums.IssueType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "issue_updates", indexes = {
        @Index(name = "idx_issue_update_complaint", columnList = "complaint_id"),
        @Index(name = "idx_issue_update_request", columnList = "request_id")
})
public class IssueUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType issueType;

    private UUID issueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    @JsonIgnore
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    @JsonIgnore
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;

    @Column(length = 50)
    private String oldStatus;

    @Column(length = 50)
    private String newStatus; // Also referred to as statusUpdate

    @Column(nullable = false, length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public IssueUpdate() {
    }

    public IssueUpdate(IssueType issueType, Complaint complaint, Request request, User updatedBy, String newStatus, String comment) {
        this.issueType = issueType;
        this.complaint = complaint;
        this.request = request;
        if (complaint != null) this.issueId = complaint.getId();
        if (request != null) this.issueId = request.getId();
        this.updatedBy = updatedBy;
        this.newStatus = newStatus;
        this.comment = comment;
    }

    public IssueUpdate(IssueType issueType, UUID issueId, User updatedBy, String oldStatus, String newStatus, String comment) {
        this.issueType = issueType;
        this.issueId = issueId;
        this.updatedBy = updatedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.comment = comment;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.complaint != null && this.issueId == null) {
            this.issueId = this.complaint.getId();
        }
        if (this.request != null && this.issueId == null) {
            this.issueId = this.request.getId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Compatibility Getters & Setters
    public String getStatusUpdate() {
        return newStatus;
    }

    public void setStatusUpdate(String statusUpdate) {
        this.newStatus = statusUpdate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Standard Getters and Setters
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

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
