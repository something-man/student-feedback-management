package com.college.feedback.dto.response;

import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;

import java.time.LocalDateTime;
import java.util.UUID;

public class PublicComplaintDto {
    private UUID id;
    private String ticketNumber;
    private String category;
    private String title;
    private String description;
    private Priority priority;
    private IssueStatus status;
    private String assignedCell;
    private LocalDateTime createdAt;
    private LocalDateTime publicPublishedAt;

    public PublicComplaintDto() {
    }

    public static PublicComplaintDto fromEntity(Complaint c) {
        if (c == null) return null;
        PublicComplaintDto dto = new PublicComplaintDto();
        dto.setId(c.getId());
        dto.setTicketNumber(c.getTicketNumber());
        dto.setCategory(c.getCategory());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());
        dto.setAssignedCell(c.getAssignedCell());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setPublicPublishedAt(c.getPublicPublishedAt() != null ? c.getPublicPublishedAt() : c.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
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

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public String getAssignedCell() {
        return assignedCell;
    }

    public void setAssignedCell(String assignedCell) {
        this.assignedCell = assignedCell;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublicPublishedAt() {
        return publicPublishedAt;
    }

    public void setPublicPublishedAt(LocalDateTime publicPublishedAt) {
        this.publicPublishedAt = publicPublishedAt;
    }
}
