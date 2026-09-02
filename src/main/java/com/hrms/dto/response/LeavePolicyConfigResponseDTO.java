package com.hrms.dto.response;

import com.hrms.entity.LeavePolicyConfigEntity;

public class LeavePolicyConfigResponseDTO {

    private Long id;
    private String leaveTypeName;
    private String employeeType;
    private String department;
    private Boolean carryForwardEnabled;
    private Integer carryForwardPercentOfAnnual;
    private Integer carryForwardMaxDays;
    private Integer carryForwardExpiryMonths;
    private Boolean encashmentAllowed;
    private Double compOffCreditDaysPerOccurrence;
    private Integer compOffExpiryMonths;
    private Boolean requiresManagerApprovalBeforeHr;

    public static LeavePolicyConfigResponseDTO from(LeavePolicyConfigEntity e) {
        LeavePolicyConfigResponseDTO dto = new LeavePolicyConfigResponseDTO();
        dto.id = e.getId();
        dto.leaveTypeName = e.getLeaveTypeName();
        dto.employeeType = e.getEmployeeType();
        dto.department = e.getDepartment();
        dto.carryForwardEnabled = e.getCarryForwardEnabled();
        dto.carryForwardPercentOfAnnual = e.getCarryForwardPercentOfAnnual();
        dto.carryForwardMaxDays = e.getCarryForwardMaxDays();
        dto.carryForwardExpiryMonths = e.getCarryForwardExpiryMonths();
        dto.encashmentAllowed = e.getEncashmentAllowed();
        dto.compOffCreditDaysPerOccurrence = e.getCompOffCreditDaysPerOccurrence();
        dto.compOffExpiryMonths = e.getCompOffExpiryMonths();
        dto.requiresManagerApprovalBeforeHr = e.getRequiresManagerApprovalBeforeHr();
        return dto;
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
}