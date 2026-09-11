package com.college.feedback.dto.request;

import java.time.LocalDateTime;

public class ReportGenerateRequest {
    private String reportType; // OVERALL, FACULTY, COURSE, INFRASTRUCTURE, COMPLAINT, REQUEST, AI_INSIGHTS
    private String category;
    private String department;
    private String term;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ReportGenerateRequest() {
    }

    public ReportGenerateRequest(String reportType, String category, String department, String term, LocalDateTime startDate, LocalDateTime endDate) {
        this.reportType = reportType;
        this.category = category;
        this.department = department;
        this.term = term;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
