package com.hrms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * "Comp Off is earned only when an employee works on an approved weekly
 * off or public holiday" — one row per such worked occurrence.
 * creditedDays and expiryDate come from LeavePolicyConfigEntity
 * (compOffCreditDaysPerOccurrence / compOffExpiryMonths) — never hardcoded.
 */
@Entity
@Table(
        name = "comp_off_earned",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empId", "workedDate"})
)
public class CompOffEarnedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String empId;

    /** Name of the leave type this Comp Off is booked under (e.g. "Comp Off") —
     * stored on the entry itself so lazy-expiry can correctly reduce the right
     * EmployeeLeaveBalanceEntity bucket without needing an external parameter. */
    @Column(nullable = false)
    private String leaveTypeName;

    /** The weekly-off / public-holiday date the employee actually worked. */
    @Column(nullable = false)
    private LocalDate workedDate;

    /** Free-text reason HR/manager gave when crediting: "Diwali", "Sunday coverage", etc. */
    @Column(length = 300)
    private String reason;

    @Column(nullable = false)
    private double creditedDays;

    /** Null => never expires (policy silent on comp-off expiry unless configured). */
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE | USED | EXPIRED

    /** Links to the LeaveRequestEntity.id that consumed this comp-off, once used. */
    private Long usedInLeaveRequestId;

    private String creditedBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        if (this.status == null) this.status = "AVAILABLE";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public LocalDate getWorkedDate() { return workedDate; }
    public void setWorkedDate(LocalDate workedDate) { this.workedDate = workedDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public double getCreditedDays() { return creditedDays; }
    public void setCreditedDays(double creditedDays) { this.creditedDays = creditedDays; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getUsedInLeaveRequestId() { return usedInLeaveRequestId; }
    public void setUsedInLeaveRequestId(Long usedInLeaveRequestId) { this.usedInLeaveRequestId = usedInLeaveRequestId; }
    public String getCreditedBy() { return creditedBy; }
    public void setCreditedBy(String creditedBy) { this.creditedBy = creditedBy; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}
