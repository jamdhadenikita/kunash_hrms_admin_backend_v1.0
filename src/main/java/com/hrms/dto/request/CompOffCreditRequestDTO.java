package com.hrms.dto.request;

import java.time.LocalDate;

/**
 * Used in: POST /api/leave/comp-off/credit
 * leaveTypeName lets HR point this at whatever they named the Comp-Off
 * type in LeaveTypeEntity — nothing hardcoded on the backend.
 */
public class CompOffCreditRequestDTO {

    private String empId;
    private String leaveTypeName;
    private LocalDate workedDate;
    private String reason;
    private String creditedBy;

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public LocalDate getWorkedDate() { return workedDate; }
    public void setWorkedDate(LocalDate workedDate) { this.workedDate = workedDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreditedBy() { return creditedBy; }
    public void setCreditedBy(String creditedBy) { this.creditedBy = creditedBy; }
}