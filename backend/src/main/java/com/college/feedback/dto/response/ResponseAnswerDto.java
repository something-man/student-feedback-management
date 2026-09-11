package com.college.feedback.dto.response;

import com.college.feedback.entity.ResponseAnswer;
import java.util.UUID;

public class ResponseAnswerDto {
    private UUID id;
    private UUID questionId;
    private String questionText;
    private Integer rating;
    private String answer;

    public ResponseAnswerDto() {
    }

    public static ResponseAnswerDto fromEntity(ResponseAnswer ans) {
        if (ans == null) return null;
        ResponseAnswerDto dto = new ResponseAnswerDto();
        dto.setId(ans.getId());
        if (ans.getQuestion() != null) {
            dto.setQuestionId(ans.getQuestion().getId());
            dto.setQuestionText(ans.getQuestion().getQuestionText());
        }
        dto.setRating(ans.getRating());
        dto.setAnswer(ans.getAnswer());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getRatingValue() {
        return rating;
    }

    public void setRatingValue(Integer ratingValue) {
        this.rating = ratingValue;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTextAnswer() {
        return answer;
    }

    public void setTextAnswer(String textAnswer) {
        this.answer = textAnswer;
    }
}
