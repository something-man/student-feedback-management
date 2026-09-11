package com.college.feedback.dto.response;

import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.Priority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ComplaintDto {
    private UUID id;
    private String ticketNumber;
    private UUID studentId;
    private String studentName;
    private String studentIdentifier;
    private String category;
    private String title;
    private String subject; // Alias for title
    private String description;
    private Priority priority;
    private IssueStatus status;
    private UUID assignedToId;
    private String assignedToName;
    private String assignedCell;
    private String adminNote;
    private Boolean publicVisible;
    private LocalDateTime publicPublishedAt;
    private LocalDateTime publicRemovedAt;
    private LocalDateTime targetResolutionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime verifiedAt;
    private List<IssueUpdateDto> updates = new ArrayList<>();

    public ComplaintDto() {
    }

    public static ComplaintDto fromEntity(Complaint c) {
        if (c == null) return null;
        ComplaintDto dto = new ComplaintDto();
        dto.setId(c.getId());
        dto.setTicketNumber(c.getTicketNumber());
        if (c.getStudent() != null) {
            dto.setStudentId(c.getStudent().getId());
            dto.setStudentName(c.getStudent().getFullName());
            dto.setStudentIdentifier(c.getStudent().getIdentifier());
        }
        dto.setCategory(c.getCategory());
        dto.setTitle(c.getTitle());
        dto.setSubject(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());
        if (c.getAssignedTo() != null) {
            dto.setAssignedToId(c.getAssignedTo().getId());
            dto.setAssignedToName(c.getAssignedTo().getFullName());
        }
        dto.setAssignedCell(c.getAssignedCell());
        dto.setAdminNote(c.getAdminNote());
        dto.setPublicVisible(c.getPublicVisible());
        dto.setPublicPublishedAt(c.getPublicPublishedAt());
        dto.setPublicRemovedAt(c.getPublicRemovedAt());
        dto.setTargetResolutionTime(c.getTargetResolutionTime());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setResolvedAt(c.getResolvedAt());
        dto.setVerifiedAt(c.getVerifiedAt());
        if (c.getUpdates() != null) {
            dto.setUpdates(c.getUpdates().stream()
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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
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
        this.subject = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        this.title = subject;
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

    public Boolean getPublicVisible() {
        return publicVisible;
    }

    public void setPublicVisible(Boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    public LocalDateTime getPublicPublishedAt() {
        return publicPublishedAt;
    }

    public void setPublicPublishedAt(LocalDateTime publicPublishedAt) {
        this.publicPublishedAt = publicPublishedAt;
    }

    public LocalDateTime getPublicRemovedAt() {
        return publicRemovedAt;
    }

    public void setPublicRemovedAt(LocalDateTime publicRemovedAt) {
        this.publicRemovedAt = publicRemovedAt;
    }

    public LocalDateTime getTargetResolutionTime() {
        return targetResolutionTime;
    }

    public void setTargetResolutionTime(LocalDateTime targetResolutionTime) {
        this.targetResolutionTime = targetResolutionTime;
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

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public List<IssueUpdateDto> getUpdates() {
        return updates;
    }

    public void setUpdates(List<IssueUpdateDto> updates) {
        this.updates = updates;
    }
}
