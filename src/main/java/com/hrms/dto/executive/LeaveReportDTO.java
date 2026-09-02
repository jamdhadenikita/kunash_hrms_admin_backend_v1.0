package com.hrms.dto.executive;

import java.util.Map;

// LeaveReportDTO.java
public class LeaveReportDTO {
    private int totalApprovedDays;
    private long pendingRequests;
    private long onLeaveToday;
    private Map<String, Long> leaveTypeBreakdown;

    public LeaveReportDTO(int totalApprovedDays, long pendingRequests,
                          long onLeaveToday, Map<String, Long> leaveTypeBreakdown) {
        this.totalApprovedDays = totalApprovedDays;
        this.pendingRequests = pendingRequests;
        this.onLeaveToday = onLeaveToday;
        this.leaveTypeBreakdown = leaveTypeBreakdown;
    }
    public int getTotalApprovedDays() { return totalApprovedDays; }
    public long getPendingRequests() { return pendingRequests; }
    public long getOnLeaveToday() { return onLeaveToday; }
    public Map<String, Long> getLeaveTypeBreakdown() { return leaveTypeBreakdown; }
}