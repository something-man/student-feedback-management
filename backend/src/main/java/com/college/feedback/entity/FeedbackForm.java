package com.college.feedback.entity;

import com.college.feedback.entity.enums.FormStatus;
import com.college.feedback.entity.enums.TargetAudience;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "feedback_forms", indexes = {
        @Index(name = "idx_form_status", columnList = "status"),
        @Index(name = "idx_form_category", columnList = "category")
})
public class FeedbackForm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String category; // COURSE, FACULTY, INFRASTRUCTURE, HOSTEL, LIBRARY, ACTIVITY, GENERAL

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormStatus status = FormStatus.PUBLISHED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetAudience targetAudience = TargetAudience.STUDENTS;

    @Column(length = 100)
    private String targetDepartment;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean allowAnonymous = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackAssignment> assignments = new ArrayList<>();

    public FeedbackForm() {
    }

    public FeedbackForm(String title, String category, TargetAudience targetAudience, String targetDepartment, LocalDateTime deadline, Boolean allowAnonymous, User createdBy) {
        this.title = title;
        this.category = category;
        this.targetAudience = targetAudience != null ? targetAudience : TargetAudience.STUDENTS;
        this.targetDepartment = targetDepartment;
        this.endDate = deadline;
        this.allowAnonymous = allowAnonymous != null ? allowAnonymous : true;
        this.createdBy = createdBy;
        this.isActive = true;
        this.status = FormStatus.PUBLISHED;
    }

    public FeedbackForm(String title, String description, String category, FormStatus status, TargetAudience targetAudience, String targetDepartment, LocalDateTime startDate, LocalDateTime endDate, Boolean allowAnonymous, User createdBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status != null ? status : FormStatus.PUBLISHED;
        this.targetAudience = targetAudience != null ? targetAudience : TargetAudience.STUDENTS;
        this.targetDepartment = targetDepartment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allowAnonymous = allowAnonymous != null ? allowAnonymous : true;
        this.createdBy = createdBy;
        this.isActive = (this.status == FormStatus.PUBLISHED);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.startDate == null) {
            this.startDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = FormStatus.PUBLISHED;
        }
        if (this.isActive == null) {
            this.isActive = (this.status == FormStatus.PUBLISHED);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.isActive = (this.status == FormStatus.PUBLISHED);
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setForm(this);
    }

    // Compatibility getter/setter for deadline
    public LocalDateTime getDeadline() {
        return endDate;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.endDate = deadline;
    }

    // Standard Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public FormStatus getStatus() {
        return status;
    }

    public void setStatus(FormStatus status) {
        this.status = status;
        this.isActive = (status == FormStatus.PUBLISHED);
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience targetAudience) {
        this.targetAudience = targetAudience;
    }

    public String getTargetDepartment() {
        return targetDepartment;
    }

    public void setTargetDepartment(String targetDepartment) {
        this.targetDepartment = targetDepartment;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<FeedbackAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<FeedbackAssignment> assignments) {
        this.assignments = assignments;
    }
}
