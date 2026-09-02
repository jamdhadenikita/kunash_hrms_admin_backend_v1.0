package com.hrms.dto.executive;

import java.util.Map;

// AssetReportDTO.java
public class AssetReportDTO {
    private Map<String, Long> statusWise;
    private Map<String, Long> categoryWise;
    private long overdueReturns;

    public AssetReportDTO(Map<String, Long> statusWise, Map<String, Long> categoryWise, long overdueReturns) {
        this.statusWise = statusWise;
        this.categoryWise = categoryWise;
        this.overdueReturns = overdueReturns;
    }
    public Map<String, Long> getStatusWise() { return statusWise; }
    public Map<String, Long> getCategoryWise() { return categoryWise; }
    public long getOverdueReturns() { return overdueReturns; }
}