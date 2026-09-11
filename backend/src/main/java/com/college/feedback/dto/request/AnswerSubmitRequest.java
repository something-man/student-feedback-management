package com.college.feedback.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AnswerSubmitRequest {
    @NotNull(message = "Question ID is required")
    private UUID questionId;

    private Integer ratingValue; // 1 to 5 for rating questions

    private String textAnswer;

    public AnswerSubmitRequest() {
    }

    public AnswerSubmitRequest(UUID questionId, Integer ratingValue, String textAnswer) {
        this.questionId = questionId;
        this.ratingValue = ratingValue;
        this.textAnswer = textAnswer;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }

    public Integer getRating() {
        return ratingValue;
    }

    public void setRating(Integer rating) {
        this.ratingValue = rating;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public String getAnswer() {
        return textAnswer;
    }

    public void setAnswer(String answer) {
        this.textAnswer = answer;
    }
}
