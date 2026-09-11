package com.college.feedback.dto.response;

import com.college.feedback.entity.Question;
import com.college.feedback.entity.enums.QuestionType;
import java.util.UUID;

public class QuestionDto {
    private UUID id;
    private String questionText;
    private QuestionType questionType;
    private Boolean required = true;
    private Integer displayOrder;

    public QuestionDto() {
    }

    public static QuestionDto fromEntity(Question question) {
        if (question == null) return null;
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setRequired(question.getRequired() != null ? question.getRequired() : true);
        dto.setDisplayOrder(question.getDisplayOrder());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
