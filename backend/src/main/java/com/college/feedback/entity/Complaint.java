package com.college.feedback.entity;

import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaint_ticket", columnList = "ticketNumber"),
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_priority", columnList = "priority"),
        @Index(name = "idx_complaint_student", columnList = "student_id")
})
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String category; // ACADEMIC, INFRASTRUCTURE, HOSTEL, LIBRARY, TRANSPORT, IT, OTHER

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title; // Also referred to as subject

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status = IssueStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(length = 100)
    private String assignedCell;

    @Column(length = 1000)
    private String adminNote;

    // Public Complaint Workflow Fields
    @Column(nullable = false)
    private Boolean publicVisible = false;

    private LocalDateTime publicPublishedAt;

    private LocalDateTime publicRemovedAt;

    private LocalDateTime targetResolutionTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime verifiedAt;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IssueUpdate> updates = new ArrayList<>();

    public Complaint() {
    }

    public Complaint(String ticketNumber, User student, String category, String title, String description, Priority priority, IssueStatus status) {
        this.ticketNumber = ticketNumber;
        this.student = student;
        this.category = category;
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : IssueStatus.PENDING;
        this.publicVisible = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.publicVisible == null) {
            this.publicVisible = false;
        }
        if (this.targetResolutionTime == null) {
            // Default 48-hour resolution target
            this.targetResolutionTime = LocalDateTime.now().plusHours(48);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addUpdate(IssueUpdate update) {
        updates.add(update);
        update.setComplaint(this);
    }

    // Compatibility getter/setter for subject
    public String getSubject() {
        return title;
    }

    public void setSubject(String subject) {
        this.title = subject;
    }

    // Standard Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
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

    public LocalDateTime getPublicPublishedAt() {
        return publicPublishedAt;
    }

    public void setPublicPublishedAt(LocalDateTime publicPublishedAt) {
        this.publicPublishedAt = publicPublishedAt;
    }

    public LocalDateTime getPublicRemovedAt() {
        return publicRemovedAt;
    }

    public void setPublicRemovedAt(LocalDateTime publicRemovedAt) {
        this.publicRemovedAt = publicRemovedAt;
    }

    public LocalDateTime getTargetResolutionTime() {
        return targetResolutionTime;
    }

    public void setTargetResolutionTime(LocalDateTime targetResolutionTime) {
        this.targetResolutionTime = targetResolutionTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public List<IssueUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<IssueUpdate> updates) {
        this.updates = updates;
    }
}
