package com.college.feedback.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class FeedbackResponseRequest {
    private Boolean isAnonymous = false;
    private Double overallRating;

    @NotEmpty(message = "Answers list cannot be empty")
    private List<AnswerSubmitRequest> answers = new ArrayList<>();

    public FeedbackResponseRequest() {
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.isAnonymous = anonymous;
    }

    public Double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Double overallRating) {
        this.overallRating = overallRating;
    }

    public List<AnswerSubmitRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmitRequest> answers) {
        this.answers = answers;
    }
}
