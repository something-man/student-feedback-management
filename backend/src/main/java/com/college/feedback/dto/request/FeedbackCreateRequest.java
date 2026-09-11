package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.FormStatus;
import com.college.feedback.entity.enums.TargetAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String category; // COURSE, FACULTY, INFRASTRUCTURE, HOSTEL, LIBRARY, ACTIVITY, GENERAL

    private FormStatus status = FormStatus.PUBLISHED;

    @NotNull(message = "Target audience is required")
    private TargetAudience targetAudience = TargetAudience.STUDENTS;

    private String targetDepartment;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime deadline; // Alias for endDate

    private Boolean allowAnonymous = true;

    private List<QuestionCreateRequest> questions = new ArrayList<>();

    public FeedbackCreateRequest() {
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
        return endDate != null ? endDate : deadline;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        this.deadline = endDate;
    }

    public LocalDateTime getDeadline() {
        return deadline != null ? deadline : endDate;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        this.endDate = deadline;
    }

    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    public void setAllowAnonymous(Boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public List<QuestionCreateRequest> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionCreateRequest> questions) {
        this.questions = questions;
    }
}
