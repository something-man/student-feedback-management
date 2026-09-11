package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionCreateRequest {
    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionType questionType = QuestionType.RATING;

    private Boolean required = true;

    private Integer displayOrder = 1;

    public QuestionCreateRequest() {
    }

    public QuestionCreateRequest(String questionText, QuestionType questionType, Integer displayOrder) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.required = true;
        this.displayOrder = displayOrder;
    }

    public QuestionCreateRequest(String questionText, QuestionType questionType, Boolean required, Integer displayOrder) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.required = required != null ? required : true;
        this.displayOrder = displayOrder;
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
