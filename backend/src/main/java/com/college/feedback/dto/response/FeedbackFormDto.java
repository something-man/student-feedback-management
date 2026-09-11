package com.college.feedback.dto.response;

import com.college.feedback.entity.FeedbackForm;
import com.college.feedback.entity.enums.AssignmentStatus;
import com.college.feedback.entity.enums.FormStatus;
import com.college.feedback.entity.enums.TargetAudience;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FeedbackFormDto {
    private UUID id;
    private String title;
    private String description;
    private String category;
    private FormStatus status;
    private TargetAudience targetAudience;
    private String targetDepartment;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime deadline; // Alias for endDate
    private Boolean isActive;
    private Boolean allowAnonymous;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionDto> questions = new ArrayList<>();
    private Long totalResponses;
    private Double averageRating;
    private AssignmentStatus userStatus; // PENDING or COMPLETED for the requesting user

    public FeedbackFormDto() {
    }

    public static FeedbackFormDto fromEntity(FeedbackForm form) {
        if (form == null) return null;
        FeedbackFormDto dto = new FeedbackFormDto();
        dto.setId(form.getId());
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setCategory(form.getCategory());
        dto.setStatus(form.getStatus());
        dto.setTargetAudience(form.getTargetAudience());
        dto.setTargetDepartment(form.getTargetDepartment());
        dto.setStartDate(form.getStartDate());
        dto.setEndDate(form.getEndDate());
        dto.setDeadline(form.getDeadline());
        dto.setIsActive(form.getIsActive());
        dto.setAllowAnonymous(form.getAllowAnonymous());
        if (form.getCreatedBy() != null) {
            dto.setCreatedByName(form.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());
        if (form.getQuestions() != null) {
            dto.setQuestions(form.getQuestions().stream()
                    .map(QuestionDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    // Getters and Setters
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
        this.deadline = endDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        this.endDate = deadline;
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

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
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

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }

    public Long getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(Long totalResponses) {
        this.totalResponses = totalResponses;
    }

    public Long getResponseCount() {
        return totalResponses;
    }

    public void setResponseCount(Long responseCount) {
        this.totalResponses = responseCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Double getAvgRating() {
        return averageRating;
    }

    public void setAvgRating(Double avgRating) {
        this.averageRating = avgRating;
    }

    public AssignmentStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(AssignmentStatus userStatus) {
        this.userStatus = userStatus;
    }
}
