package com.college.feedback.dto.response;

import java.util.List;

public class SentimentAnalysisDto {
    private Double positivePercent;
    private Double neutralPercent;
    private Double negativePercent;
    private Long totalAnalyzed;
    private Long positiveCount;
    private Long neutralCount;
    private Long negativeCount;
    private Double sentimentScore;
    private List<String> topKeywords;
    private String summary;

    public SentimentAnalysisDto() {
    }

    public SentimentAnalysisDto(Double positivePercent, Double neutralPercent, Double negativePercent,
                                Long totalAnalyzed, Long positiveCount, Long neutralCount, Long negativeCount,
                                Double sentimentScore, List<String> topKeywords, String summary) {
        this.positivePercent = positivePercent;
        this.neutralPercent = neutralPercent;
        this.negativePercent = negativePercent;
        this.totalAnalyzed = totalAnalyzed;
        this.positiveCount = positiveCount;
        this.neutralCount = neutralCount;
        this.negativeCount = negativeCount;
        this.sentimentScore = sentimentScore;
        this.topKeywords = topKeywords;
        this.summary = summary;
    }

    public Double getPositivePercent() {
        return positivePercent;
    }

    public void setPositivePercent(Double positivePercent) {
        this.positivePercent = positivePercent;
    }

    public Double getNeutralPercent() {
        return neutralPercent;
    }

    public void setNeutralPercent(Double neutralPercent) {
        this.neutralPercent = neutralPercent;
    }

    public Double getNegativePercent() {
        return negativePercent;
    }

    public void setNegativePercent(Double negativePercent) {
        this.negativePercent = negativePercent;
    }

    public Long getTotalAnalyzed() {
        return totalAnalyzed;
    }

    public void setTotalAnalyzed(Long totalAnalyzed) {
        this.totalAnalyzed = totalAnalyzed;
    }

    public Long getPositiveCount() {
        return positiveCount;
    }

    public void setPositiveCount(Long positiveCount) {
        this.positiveCount = positiveCount;
    }

    public Long getNeutralCount() {
        return neutralCount;
    }

    public void setNeutralCount(Long neutralCount) {
        this.neutralCount = neutralCount;
    }

    public Long getNegativeCount() {
        return negativeCount;
    }

    public void setNegativeCount(Long negativeCount) {
        this.negativeCount = negativeCount;
    }

    public Double getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(Double sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public List<String> getTopKeywords() {
        return topKeywords;
    }

    public void setTopKeywords(List<String> topKeywords) {
        this.topKeywords = topKeywords;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
