package com.hrms.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public class AdminResponseDto {

    private String adminId;
    private String adminFirstName;
    private String adminLastName;
    private String adminMobileNumber;
    private String adminAddress;
    private String adminRole;
    private String adminDepartment;
    private List<String> moduleAccess;

    public AdminResponseDto(){}

    // 7-param constructor (old, keep if used anywhere)
    public AdminResponseDto(String adminId, String adminFirstName, String adminLastName,
                            String adminMobileNumber, String adminAddress,
                            String adminRole, String adminDepartment) {
        this.adminId = adminId;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminMobileNumber = adminMobileNumber;
        this.adminAddress = adminAddress;
        this.adminRole = adminRole;
        this.adminDepartment = adminDepartment;
    }

    // ── NEW: 8-param constructor (with moduleAccess) — THIS FIXES THE ERROR ──
    public AdminResponseDto(String adminId, String adminFirstName, String adminLastName,
                            String adminMobileNumber, String adminAddress,
                            String adminRole, String adminDepartment, List<String> moduleAccess) {
        this.adminId = adminId;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminMobileNumber = adminMobileNumber;
        this.adminAddress = adminAddress;
        this.adminRole = adminRole;
        this.adminDepartment = adminDepartment;
        this.moduleAccess = moduleAccess;
    }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getAdminFirstName() { return adminFirstName; }
    public void setAdminFirstName(String adminFirstName) { this.adminFirstName = adminFirstName; }

    public String getAdminLastName() { return adminLastName; }
    public void setAdminLastName(String adminLastName) { this.adminLastName = adminLastName; }

    public String getAdminMobileNumber() { return adminMobileNumber; }
    public void setAdminMobileNumber(String adminMobileNumber) { this.adminMobileNumber = adminMobileNumber; }

    public String getAdminAddress() { return adminAddress; }
    public void setAdminAddress(String adminAddress) { this.adminAddress = adminAddress; }

    public String getAdminRole() { return adminRole; }
    public void setAdminRole(String adminRole) { this.adminRole = adminRole; }

    public String getAdminDepartment() { return adminDepartment; }
    public void setAdminDepartment(String adminDepartment) { this.adminDepartment = adminDepartment; }

    public List<String> getModuleAccess() { return moduleAccess; }
    public void setModuleAccess(List<String> moduleAccess) { this.moduleAccess = moduleAccess; }
}