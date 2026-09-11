package com.college.feedback.entity;

import com.college.feedback.entity.enums.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "feedback_responses", indexes = {
        @Index(name = "idx_response_form", columnList = "form_id"),
        @Index(name = "idx_response_status", columnList = "status")
})
public class FeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable if isAnonymous = true
    private User user;

    @Column(nullable = false)
    private Boolean isAnonymous = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseStatus status = ResponseStatus.SUBMITTED;

    private Double overallRating;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ResponseAnswer> answers = new ArrayList<>();

    public FeedbackResponse() {
    }

    public FeedbackResponse(FeedbackForm form, User user, Boolean isAnonymous, Double overallRating) {
        this.form = form;
        this.user = user;
        this.isAnonymous = isAnonymous != null ? isAnonymous : false;
        this.overallRating = overallRating;
        this.status = ResponseStatus.SUBMITTED;
    }

    public FeedbackResponse(FeedbackForm form, User user, Boolean isAnonymous, ResponseStatus status, Double overallRating) {
        this.form = form;
        this.user = user;
        this.isAnonymous = isAnonymous != null ? isAnonymous : false;
        this.status = status != null ? status : ResponseStatus.SUBMITTED;
        this.overallRating = overallRating;
    }

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ResponseStatus.SUBMITTED;
        }
    }

    public void addAnswer(ResponseAnswer answer) {
        answers.add(answer);
        answer.setResponse(this);
    }

    // Alias methods
    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.isAnonymous = anonymous;
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

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public Double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Double overallRating) {
        this.overallRating = overallRating;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<ResponseAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ResponseAnswer> answers) {
        this.answers = answers;
    }
}
