package com.hrms.dto.request;

public class RegisterManagerApprovalDTO {

    private Long leaveRequestId;
    private String empId;
    private String leaveTypeName;
    private String empType;
    private String department;
    private boolean isEmergency;

    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public String getEmpType() { return empType; }
    public void setEmpType(String empType) { this.empType = empType; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean emergency) { isEmergency = emergency; }
}