package com.college.feedback.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportDataDto {
    private String reportType;
    private String title;
    private String subtitle;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private Map<String, Object> summaryMetrics;
    private List<String> headers;
    private List<List<String>> rows;
    private List<String> recommendations;

    public ReportDataDto() {
    }

    public ReportDataDto(String reportType, String title, String subtitle, LocalDateTime generatedAt,
                         String generatedBy, Map<String, Object> summaryMetrics,
                         List<String> headers, List<List<String>> rows, List<String> recommendations) {
        this.reportType = reportType;
        this.title = title;
        this.subtitle = subtitle;
        this.generatedAt = generatedAt;
        this.generatedBy = generatedBy;
        this.summaryMetrics = summaryMetrics;
        this.headers = headers;
        this.rows = rows;
        this.recommendations = recommendations;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Map<String, Object> getSummaryMetrics() {
        return summaryMetrics;
    }

    public void setSummaryMetrics(Map<String, Object> summaryMetrics) {
        this.summaryMetrics = summaryMetrics;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
