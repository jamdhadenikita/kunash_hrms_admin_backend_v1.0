package com.hrms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Dynamic, admin-configurable policy parameters for a leave type.
 *
 * NOTHING here is hardcoded in Java. Every number (carry-forward %,
 * max carry-forward days, expiry window, comp-off credit days, etc.)
 * is a row in this table that HR sets via LeavePolicyConfigController.
 * The policy document's numbers (12 days, 6 days, 6 months, etc.) are
 * only DEFAULT SEED VALUES an admin can enter — never compiled constants.
 *
 * Resolution follows the same specific-department -> "All" fallback
 * pattern already used in LeaveRuleServiceImpl.getAllottedDays().
 */
@Entity
@Table(
        name = "leave_policy_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"leaveTypeName", "employeeType", "department"})
)
public class LeavePolicyConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String leaveTypeName;   // must match LeaveTypeEntity.name

    @Column(nullable = false)
    private String employeeType;    // Full-Time | Part-Time | Contract | Intern | "All"

    @Column(nullable = false)
    private String department;      // IT | HR | ... | "All"

    // Carry-forward rules (CL-style leave types)
    @Column(nullable = false)
    private Boolean carryForwardEnabled = false;

    /** e.g. 50 => up to 50% of the annual entitlement may be carried forward. Admin-set. */
    @Column(nullable = false)
    private Integer carryForwardPercentOfAnnual = 0;

    /** Hard cap on carried-forward days regardless of percent, e.g. 6. Admin-set. */
    @Column(nullable = false)
    private Integer carryForwardMaxDays = 0;

    /** Carried-forward days must be used within this many months of the new year, else lapse. Admin-set. */
    @Column(nullable = false)
    private Integer carryForwardExpiryMonths = 0;

    // Encashment
    @Column(nullable = false)
    private Boolean encashmentAllowed = false;

    // Comp-Off specific (ignored for non comp-off leave types)
    /** Days credited per single worked weekly-off/holiday. Admin-set (e.g. 1.0, or 0.5 for half-day). */
    @Column(nullable = false)
    private Double compOffCreditDaysPerOccurrence = 0.0;

    /** Months after which an earned-but-unused Comp Off lapses. 0/null => never expires. Admin-set. */
    private Integer compOffExpiryMonths;

    // LWP specific
    /**
     * If true, this leave type requires Reporting Manager approval before HR
     * can approve — except when the request is flagged as an emergency.
     */
    @Column(nullable = false)
    private Boolean requiresManagerApprovalBeforeHr = false;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Boolean getCarryForwardEnabled() { return carryForwardEnabled; }
    public void setCarryForwardEnabled(Boolean carryForwardEnabled) { this.carryForwardEnabled = carryForwardEnabled; }

    public Integer getCarryForwardPercentOfAnnual() { return carryForwardPercentOfAnnual; }
    public void setCarryForwardPercentOfAnnual(Integer carryForwardPercentOfAnnual) { this.carryForwardPercentOfAnnual = carryForwardPercentOfAnnual; }

    public Integer getCarryForwardMaxDays() { return carryForwardMaxDays; }
    public void setCarryForwardMaxDays(Integer carryForwardMaxDays) { this.carryForwardMaxDays = carryForwardMaxDays; }

    public Integer getCarryForwardExpiryMonths() { return carryForwardExpiryMonths; }
    public void setCarryForwardExpiryMonths(Integer carryForwardExpiryMonths) { this.carryForwardExpiryMonths = carryForwardExpiryMonths; }

    public Boolean getEncashmentAllowed() { return encashmentAllowed; }
    public void setEncashmentAllowed(Boolean encashmentAllowed) { this.encashmentAllowed = encashmentAllowed; }

    public Double getCompOffCreditDaysPerOccurrence() { return compOffCreditDaysPerOccurrence; }
    public void setCompOffCreditDaysPerOccurrence(Double compOffCreditDaysPerOccurrence) { this.compOffCreditDaysPerOccurrence = compOffCreditDaysPerOccurrence; }

    public Integer getCompOffExpiryMonths() { return compOffExpiryMonths; }
    public void setCompOffExpiryMonths(Integer compOffExpiryMonths) { this.compOffExpiryMonths = compOffExpiryMonths; }

    public Boolean getRequiresManagerApprovalBeforeHr() { return requiresManagerApprovalBeforeHr; }
    public void setRequiresManagerApprovalBeforeHr(Boolean requiresManagerApprovalBeforeHr) { this.requiresManagerApprovalBeforeHr = requiresManagerApprovalBeforeHr; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}