package com.hrms.dto.response;

/**
 * Tells the frontend whether an employee is currently eligible to
 * apply for LWP (i.e. all PAID leave balances are exhausted), per policy:
 * "LWP may be granted at the discretion of Management if an employee has
 * exhausted all available leave balances."
 */
public class LwpEligibilityDTO {

    private String empId;
    private int year;
    private boolean eligible;
    private double totalRemainingPaidLeave;
    private String message;

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }
    public double getTotalRemainingPaidLeave() { return totalRemainingPaidLeave; }
    public void setTotalRemainingPaidLeave(double totalRemainingPaidLeave) { this.totalRemainingPaidLeave = totalRemainingPaidLeave; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
