package com.college.feedback.entity;

import com.college.feedback.entity.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    @JsonBackReference
    private FeedbackForm form;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String questionText;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType questionType = QuestionType.RATING;

    @Column(nullable = false)
    private Boolean required = true;

    @Column(nullable = false)
    private Integer displayOrder = 1;

    public Question() {
    }

    public Question(String questionText, QuestionType questionType, Integer displayOrder) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.displayOrder = displayOrder;
        this.required = true;
    }

    public Question(String questionText, QuestionType questionType, Boolean required, Integer displayOrder) {
        this.questionText = questionText;
        this.questionType = questionType;
        this.required = required != null ? required : true;
        this.displayOrder = displayOrder;
    }

    public Question(FeedbackForm form, String questionText, QuestionType questionType, Integer displayOrder) {
        this.form = form;
        this.questionText = questionText;
        this.questionType = questionType;
        this.displayOrder = displayOrder;
        this.required = true;
    }

    public Question(FeedbackForm form, String questionText, QuestionType questionType, Boolean required, Integer displayOrder) {
        this.form = form;
        this.questionText = questionText;
        this.questionType = questionType;
        this.required = required != null ? required : true;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
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
