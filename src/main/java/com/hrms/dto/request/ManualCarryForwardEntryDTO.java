package com.hrms.dto.request;

import java.time.LocalDate;

/**
 * Used in: POST /api/leave/carry-forward/manual-entry
 *
 * One-time migration entry — HR manually enters what an employee's
 * actual carried-forward balance is (from pre-system records), since
 * the system has no historical year tracked yet to auto-calculate from.
 */
public class ManualCarryForwardEntryDTO {

    private String empId;
    private String leaveType;
    private int toYear;
    private double carriedDays;
    private LocalDate expiryDate; // optional — computed from policy config if omitted

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public int getToYear() { return toYear; }
    public void setToYear(int toYear) { this.toYear = toYear; }
    public double getCarriedDays() { return carriedDays; }
    public void setCarriedDays(double carriedDays) { this.carriedDays = carriedDays; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}

