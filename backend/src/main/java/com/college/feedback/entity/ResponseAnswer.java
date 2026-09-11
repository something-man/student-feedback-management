package com.college.feedback.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "response_answers", indexes = {
        @Index(name = "idx_answer_response", columnList = "response_id"),
        @Index(name = "idx_answer_question", columnList = "question_id")
})
public class ResponseAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    @JsonBackReference
    private FeedbackResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private Integer rating;

    @Column(length = 2000)
    private String answer;

    public ResponseAnswer() {
    }

    public ResponseAnswer(Question question, Integer rating, String answer) {
        this.question = question;
        this.rating = rating;
        this.answer = answer;
    }

    public ResponseAnswer(FeedbackResponse response, Question question, Integer rating, String answer) {
        this.response = response;
        this.question = question;
        this.rating = rating;
        this.answer = answer;
    }

    // Compatibility Getters & Setters
    public Integer getRatingValue() {
        return rating;
    }

    public void setRatingValue(Integer ratingValue) {
        this.rating = ratingValue;
    }

    public String getTextAnswer() {
        return answer;
    }

    public void setTextAnswer(String textAnswer) {
        this.answer = textAnswer;
    }

    // Standard Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FeedbackResponse getResponse() {
        return response;
    }

    public void setResponse(FeedbackResponse response) {
        this.response = response;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
