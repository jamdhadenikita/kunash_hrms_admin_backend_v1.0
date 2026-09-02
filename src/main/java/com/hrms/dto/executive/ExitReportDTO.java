package com.hrms.dto.executive;

import java.util.Map;

// ExitReportDTO.java
public class ExitReportDTO {
    private long completedExitsThisYear;
    private Map<String, Long> departmentWiseExits;
    private Map<String, Long> reasonWiseExits;
    private Double avgTenureDays;

    public ExitReportDTO(long completedExitsThisYear, Map<String, Long> departmentWiseExits,
                         Map<String, Long> reasonWiseExits, Double avgTenureDays) {
        this.completedExitsThisYear = completedExitsThisYear;
        this.departmentWiseExits = departmentWiseExits;
        this.reasonWiseExits = reasonWiseExits;
        this.avgTenureDays = avgTenureDays;
    }
    public long getCompletedExitsThisYear() { return completedExitsThisYear; }
    public Map<String, Long> getDepartmentWiseExits() { return departmentWiseExits; }
    public Map<String, Long> getReasonWiseExits() { return reasonWiseExits; }
    public Double getAvgTenureDays() { return avgTenureDays; }
}