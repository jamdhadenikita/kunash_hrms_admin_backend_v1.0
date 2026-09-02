package com.hrms.dto.executive;

// AttendanceReportDTO.java
public class AttendanceReportDTO {
    private double attendancePercentage;
    private long totalRecords;
    private long presentCount;
    private long wfhCount;

    public AttendanceReportDTO(long presentCount, long totalRecords, long wfhCount) {
        this.presentCount = presentCount;
        this.totalRecords = totalRecords;
        this.wfhCount = wfhCount;
        this.attendancePercentage = totalRecords == 0 ? 0.0 :
                Math.round((presentCount * 10000.0) / totalRecords) / 100.0;
    }
    public double getAttendancePercentage() { return attendancePercentage; }
    public long getTotalRecords() { return totalRecords; }
    public long getPresentCount() { return presentCount; }
    public long getWfhCount() { return wfhCount; }
}