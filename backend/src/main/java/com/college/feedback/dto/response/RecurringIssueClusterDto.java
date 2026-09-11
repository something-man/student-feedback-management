package com.college.feedback.dto.response;

import java.util.List;
import java.util.UUID;

public class RecurringIssueClusterDto {
    private String clusterId;
    private String title;
    private String category;
    private String priority;
    private Long count;
    private String description;
    private List<String> sampleLocations;
    private List<String> sampleTickets;
    private UUID sampleComplaintId;

    public RecurringIssueClusterDto() {
    }

    public RecurringIssueClusterDto(String clusterId, String title, String category, String priority,
                                    Long count, String description, List<String> sampleLocations,
                                    List<String> sampleTickets, UUID sampleComplaintId) {
        this.clusterId = clusterId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.count = count;
        this.description = description;
        this.sampleLocations = sampleLocations;
        this.sampleTickets = sampleTickets;
        this.sampleComplaintId = sampleComplaintId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSampleLocations() {
        return sampleLocations;
    }

    public void setSampleLocations(List<String> sampleLocations) {
        this.sampleLocations = sampleLocations;
    }

    public List<String> getSampleTickets() {
        return sampleTickets;
    }

    public void setSampleTickets(List<String> sampleTickets) {
        this.sampleTickets = sampleTickets;
    }

    public UUID getSampleComplaintId() {
        return sampleComplaintId;
    }

    public void setSampleComplaintId(UUID sampleComplaintId) {
        this.sampleComplaintId = sampleComplaintId;
    }
}
