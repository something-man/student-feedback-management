package com.college.feedback.dto.response;

import com.college.feedback.entity.Request;
import com.college.feedback.entity.enums.RequestStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RequestDto {
    private UUID id;
    private String requestNumber;
    private UUID studentId;
    private String studentName;
    private String studentIdentifier;
    private String category;
    private String title;
    private String details;
    private String description; // Alias
    private RequestStatus status;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedCell;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private List<IssueUpdateDto> updates = new ArrayList<>();

    public RequestDto() {
    }

    public static RequestDto fromEntity(Request r) {
        if (r == null) return null;
        RequestDto dto = new RequestDto();
        dto.setId(r.getId());
        dto.setRequestNumber(r.getRequestNumber());
        if (r.getStudent() != null) {
            dto.setStudentId(r.getStudent().getId());
            dto.setStudentName(r.getStudent().getFullName());
            dto.setStudentIdentifier(r.getStudent().getIdentifier());
        }
        dto.setCategory(r.getCategory());
        dto.setTitle(r.getTitle());
        dto.setDetails(r.getDescription());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        if (r.getAssignedTo() != null) {
            dto.setAssignedToId(r.getAssignedTo().getId());
            dto.setAssignedToName(r.getAssignedTo().getFullName());
        }
        dto.setAssignedCell(r.getAssignedCell());
        dto.setAdminNote(r.getAdminNote());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        dto.setResolvedAt(r.getResolvedAt());
        if (r.getUpdates() != null) {
            dto.setUpdates(r.getUpdates().stream()
                    .map(IssueUpdateDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentIdentifier() {
        return studentIdentifier;
    }

    public void setStudentIdentifier(String studentIdentifier) {
        this.studentIdentifier = studentIdentifier;
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
        this.description = details;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.details = description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getAssignedCell() {
        return assignedCell;
    }

    public void setAssignedCell(String assignedCell) {
        this.assignedCell = assignedCell;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<IssueUpdateDto> getUpdates() {
        return updates;
    }

    public void setUpdates(List<IssueUpdateDto> updates) {
        this.updates = updates;
    }
}
