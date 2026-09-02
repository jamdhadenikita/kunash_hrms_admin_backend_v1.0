package com.hrms.dto.request;

/**
 * Request DTO to create/update policy config for a leave type.
 * Used in: POST /api/leave/policy-config
 *
 * Every value here is admin-entered — nothing is a Java constant.
 * Example body:
 * {
 *   "leaveTypeName": "Casual Leave",
 *   "employeeType": "All",
 *   "department": "All",
 *   "carryForwardEnabled": true,
 *   "carryForwardPercentOfAnnual": 50,
 *   "carryForwardMaxDays": 6,
 *   "carryForwardExpiryMonths": 6,
 *   "encashmentAllowed": false,
 *   "compOffCreditDaysPerOccurrence": 0,
 *   "compOffExpiryMonths": null,
 *   "requiresManagerApprovalBeforeHr": false
 * }
 */
public class LeavePolicyConfigRequestDTO {

    private String leaveTypeName;
    private String employeeType;   // defaults to "All" if blank
    private String department;     // defaults to "All" if blank

    private Boolean carryForwardEnabled;
    private Integer carryForwardPercentOfAnnual;
    private Integer carryForwardMaxDays;
    private Integer carryForwardExpiryMonths;

    private Boolean encashmentAllowed;

    private Double compOffCreditDaysPerOccurrence;
    private Integer compOffExpiryMonths;

    private Boolean requiresManagerApprovalBeforeHr;

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

