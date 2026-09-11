package com.college.feedback.dto.request;

import com.college.feedback.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ComplaintCreateRequest {
    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.MEDIUM;

    public ComplaintCreateRequest() {
    }

    public ComplaintCreateRequest(String category, String subject, String description, Priority priority) {
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return subject;
    }

    public void setTitle(String title) {
        this.subject = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
