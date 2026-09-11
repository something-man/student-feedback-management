package com.college.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RequestCreateRequest {
    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Details are required")
    private String details;

    public RequestCreateRequest() {
    }

    public RequestCreateRequest(String category, String title, String details) {
        this.category = category;
        this.title = title;
        this.details = details;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
