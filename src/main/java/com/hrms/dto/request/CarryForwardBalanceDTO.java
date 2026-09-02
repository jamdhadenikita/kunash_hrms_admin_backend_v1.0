package com.hrms.dto.response;

import java.time.LocalDate;

/**
 * Shown alongside the regular balance so employees/HR can see the
 * carried-forward bucket and its expiry separately from the fresh
 * yearly allotment (they behave differently — carried days lapse
 * after N months, fresh days follow the normal year cycle).
 */
public class CarryForwardBalanceDTO {

    private String empId;
    private String leaveType;
    private int fromYear;
    private int toYear;
    private double carriedDays;
    private double usedFromCarry;
    private double remainingCarry;
    private LocalDate expiryDate;
    private String status; // ACTIVE | LAPSED | FULLY_USED

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public int getFromYear() { return fromYear; }
    public void setFromYear(int fromYear) { this.fromYear = fromYear; }
    public int getToYear() { return toYear; }
    public void setToYear(int toYear) { this.toYear = toYear; }
    public double getCarriedDays() { return carriedDays; }
    public void setCarriedDays(double carriedDays) { this.carriedDays = carriedDays; }
    public double getUsedFromCarry() { return usedFromCarry; }
    public void setUsedFromCarry(double usedFromCarry) { this.usedFromCarry = usedFromCarry; }
    public double getRemainingCarry() { return remainingCarry; }
    public void setRemainingCarry(double remainingCarry) { this.remainingCarry = remainingCarry; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}