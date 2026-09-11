package com.college.feedback.entity;

import com.college.feedback.entity.enums.AssignmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"form_id", "user_id"})
}, indexes = {
    @Index(name = "idx_assignment_user", columnList = "user_id"),
    @Index(name = "idx_assignment_status", columnList = "status")
})
public class FeedbackAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Assigned To

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedDate;

    private LocalDateTime deadline;

    private LocalDateTime completedAt;

    public FeedbackAssignment() {
    }

    public FeedbackAssignment(FeedbackForm form, User user) {
        this.form = form;
        this.user = user;
        this.status = AssignmentStatus.PENDING;
        if (form != null) {
            this.assignedBy = form.getCreatedBy();
            this.deadline = form.getDeadline();
        }
    }

    public FeedbackAssignment(FeedbackForm form, User user, User assignedBy, LocalDateTime deadline) {
        this.form = form;
        this.user = user;
        this.assignedBy = assignedBy;
        this.deadline = deadline;
        this.status = AssignmentStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.assignedDate = LocalDateTime.now();
        if (this.deadline == null && this.form != null) {
            this.deadline = this.form.getDeadline();
        }
    }

    // Alias methods
    public User getAssignedTo() {
        return user;
    }

    public void setAssignedTo(User assignedTo) {
        this.user = assignedTo;
    }

    // Standard Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FeedbackForm getForm() {
        return form;
    }

    public void setForm(FeedbackForm form) {
        this.form = form;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(User assignedBy) {
        this.assignedBy = assignedBy;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getAssignedAt() {
        return assignedDate;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedDate = assignedAt;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
