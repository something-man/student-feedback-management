package com.college.feedback.dto.response;

import com.college.feedback.entity.FeedbackResponse;
import com.college.feedback.entity.enums.ResponseStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FeedbackResponseDto {
    private UUID id;
    private UUID formId;
    private String formTitle;
    private Boolean isAnonymous;
    private ResponseStatus status;
    private Double overallRating;
    private LocalDateTime submittedAt;
    private List<ResponseAnswerDto> answers = new ArrayList<>();

    // User metadata (Strictly masked / nullified when isAnonymous = true)
    private UUID userId;
    private String userName;
    private String userIdentifier;
    private String userEmail;

    public FeedbackResponseDto() {
    }

    public static FeedbackResponseDto fromEntity(FeedbackResponse r) {
        if (r == null) return null;
        FeedbackResponseDto dto = new FeedbackResponseDto();
        dto.setId(r.getId());
        if (r.getForm() != null) {
            dto.setFormId(r.getForm().getId());
            dto.setFormTitle(r.getForm().getTitle());
        }
        dto.setIsAnonymous(r.getIsAnonymous());
        dto.setStatus(r.getStatus());
        dto.setOverallRating(r.getOverallRating());
        dto.setSubmittedAt(r.getSubmittedAt());

        // STRICT PRIVACY ENFORCEMENT:
        // If anonymous, never attach user identity
        if (Boolean.TRUE.equals(r.getIsAnonymous()) || r.getUser() == null) {
            dto.setUserId(null);
            dto.setUserName("Anonymous Response");
            dto.setUserIdentifier(null);
            dto.setUserEmail(null);
        } else {
            dto.setUserId(r.getUser().getId());
            dto.setUserName(r.getUser().getFullName());
            dto.setUserIdentifier(r.getUser().getIdentifier());
            dto.setUserEmail(r.getUser().getEmail());
        }

        if (r.getAnswers() != null) {
            dto.setAnswers(r.getAnswers().stream()
                    .map(ResponseAnswerDto::fromEntity)
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

    public UUID getFormId() {
        return formId;
    }

    public void setFormId(UUID formId) {
        this.formId = formId;
    }

    public String getFormTitle() {
        return formTitle;
    }

    public void setFormTitle(String formTitle) {
        this.formTitle = formTitle;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
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

    public List<ResponseAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ResponseAnswerDto> answers) {
        this.answers = answers;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
