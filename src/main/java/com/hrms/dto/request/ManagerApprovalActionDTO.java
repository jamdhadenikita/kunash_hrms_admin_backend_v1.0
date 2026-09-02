package com.hrms.dto.request;

public class ManagerApprovalActionDTO {

    private String action;      // "approve" | "reject"
    private String managerName;
    private String remarks;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}