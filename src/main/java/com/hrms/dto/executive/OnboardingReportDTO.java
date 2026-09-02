package com.hrms.dto.executive;

// OnboardingReportDTO.java
public class OnboardingReportDTO {
    private long pendingCount;
    private Double avgCompletionDays;

    public OnboardingReportDTO(long pendingCount, Double avgCompletionDays) {
        this.pendingCount = pendingCount;
        this.avgCompletionDays = avgCompletionDays;
    }
    public long getPendingCount() { return pendingCount; }
    public Double getAvgCompletionDays() { return avgCompletionDays; }
}