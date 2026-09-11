package com.college.feedback.dto.response;

import java.util.List;

public class TrendAnalyticsDto {
    private String period;
    private List<String> labels;
    private List<Long> data;
    private List<Double> avgRatings;

    public TrendAnalyticsDto() {
    }

    public TrendAnalyticsDto(String period, List<String> labels, List<Long> data, List<Double> avgRatings) {
        this.period = period;
        this.labels = labels;
        this.data = data;
        this.avgRatings = avgRatings;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }

    public List<Double> getAvgRatings() {
        return avgRatings;
    }

    public void setAvgRatings(List<Double> avgRatings) {
        this.avgRatings = avgRatings;
    }
}
