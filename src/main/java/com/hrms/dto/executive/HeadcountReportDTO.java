package com.hrms.dto.executive;

import java.util.Map;

// HeadcountReportDTO.java
public class HeadcountReportDTO {
    private long totalActive;
    private Map<String, Long> departmentWise;   // dept -> count
    private long newJoinersInRange;
    private long exitsInRange;
    private long netHeadcountChange;

    public HeadcountReportDTO(long totalActive, Map<String, Long> departmentWise,
                              long newJoinersInRange, long exitsInRange) {
        this.totalActive = totalActive;
        this.departmentWise = departmentWise;
        this.newJoinersInRange = newJoinersInRange;
        this.exitsInRange = exitsInRange;
        this.netHeadcountChange = newJoinersInRange - exitsInRange;
    }
    // getters
    public long getTotalActive() { return totalActive; }
    public Map<String, Long> getDepartmentWise() { return departmentWise; }
    public long getNewJoinersInRange() { return newJoinersInRange; }
    public long getExitsInRange() { return exitsInRange; }
    public long getNetHeadcountChange() { return netHeadcountChange; }
}