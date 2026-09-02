package com.hrms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(
        name = "employee_leave_carry_forward",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empId", "leaveType", "fromYear"})
)
public class EmployeeLeaveCarryForwardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String empId;

    @Column(nullable = false)
    private String leaveType;

    @Column(nullable = false)
    private int fromYear;   // the year the balance was carried FROM

    @Column(nullable = false)
    private int toYear;     // the year it is usable IN

    @Column(nullable = false)
    private double carriedDays;

    @Column(nullable = false)
    private double usedFromCarry;

    @Column(nullable = false)
    private LocalDate expiryDate;   // after this date, unused carriedDays lapse automatically

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE | LAPSED | FULLY_USED

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        if (this.status == null) this.status = "ACTIVE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public double getRemainingCarry() {
        return Math.max(0, carriedDays - usedFromCarry);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}
