package com.hrms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Sits alongside LeaveRequestEntity (linked by id, not a JPA relation,
 * so LeaveRequestEntity itself never needs to be touched).
 *
 * Policy: "LWP must be pre-approved by the employee's Reporting Manager
 * and the HR Department before the leave is availed, except in cases of
 * unforeseen emergencies."
 */
@Entity
@Table(name = "leave_manager_approval")
public class LeaveManagerApprovalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long leaveRequestId;   // FK to LeaveRequestEntity.id

    @Column(nullable = false)
    private String empId;

    @Column(nullable = false)
    private Boolean isEmergency = false;

    @Column(nullable = false)
    private String managerApprovalStatus = "PENDING"; // PENDING | APPROVED | REJECTED | NOT_REQUIRED

    private String managerName;
    private LocalDate managerActionDate;
    private String managerRemarks;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        if (this.managerApprovalStatus == null) this.managerApprovalStatus = "PENDING";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public Boolean getIsEmergency() { return isEmergency; }
    public void setIsEmergency(Boolean isEmergency) { this.isEmergency = isEmergency; }
    public String getManagerApprovalStatus() { return managerApprovalStatus; }
    public void setManagerApprovalStatus(String managerApprovalStatus) { this.managerApprovalStatus = managerApprovalStatus; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public LocalDate getManagerActionDate() { return managerActionDate; }
    public void setManagerActionDate(LocalDate managerActionDate) { this.managerActionDate = managerActionDate; }
    public String getManagerRemarks() { return managerRemarks; }
    public void setManagerRemarks(String managerRemarks) { this.managerRemarks = managerRemarks; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate updatedAt) { this.updatedAt = updatedAt; }
}
