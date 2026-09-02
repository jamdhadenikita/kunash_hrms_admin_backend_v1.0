package com.hrms.dto.request;

import com.hrms.enum_status.PayrollStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;


public class PayrollPatchDTO {

    // ── Earnings (any can be individually corrected) ───────────────────────
    @Positive(message = "Basic salary must be a positive value.")
    private Double basicSalary;

    // ── Deductions ────────────────────────────────────────────────────────
    @Min(value = 0, message = "Employee PF cannot be negative.")
    private Double employeePf;          // ← was: pf

    @Min(value = 0, message = "Employer PF cannot be negative.")
    private Double employerPf;


    // ── Attendance & Expense ─────────────────────────────────────────────
    private Integer workingDaysInMonth;
    private Integer daysWorked;
    private Integer lopDays;
    private Double lopDeduction;
    private Double expenseReimbursement;

    // ── State & Notes ─────────────────────────────────────────────────────
    private PayrollStatus status;
    private String remarks;

    // ── Constructors ──────────────────────────────────────────────────────
    public PayrollPatchDTO() {}

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }


    public Double getEmployeePf() {
        return employeePf;
    }

    public void setEmployeePf(Double employeePf) {
        this.employeePf = employeePf;
    }

    public Double getEmployerPf() {
        return employerPf;
    }

    public void setEmployerPf(Double employerPf) {
        this.employerPf = employerPf;
    }


    public Integer getWorkingDaysInMonth() { return workingDaysInMonth; }
    public void setWorkingDaysInMonth(Integer workingDaysInMonth) { this.workingDaysInMonth = workingDaysInMonth; }

    public Integer getDaysWorked() { return daysWorked; }
    public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }

    public Integer getLopDays() { return lopDays; }
    public void setLopDays(Integer lopDays) { this.lopDays = lopDays; }

    public Double getLopDeduction() { return lopDeduction; }
    public void setLopDeduction(Double lopDeduction) { this.lopDeduction = lopDeduction; }

    public Double getExpenseReimbursement() { return expenseReimbursement; }
    public void setExpenseReimbursement(Double expenseReimbursement) { this.expenseReimbursement = expenseReimbursement; }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

}