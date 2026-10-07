package com.hrms.dto.response;

import java.time.LocalDate;

public class BirthdayDTO {
    private String employeeId;
    private String fullName;
    private String department;
    private String designation;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth; // original DOB (with birth year)
    private LocalDate occursOn;    // this occurrence's actual calendar date (current/requested year)
    private int turningAge;

    public BirthdayDTO() {}

    public BirthdayDTO(String employeeId, String fullName, String department,
                       String designation, String profilePhotoUrl,
                       LocalDate dateOfBirth, int turningAge) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
        this.profilePhotoUrl = profilePhotoUrl;
        this.dateOfBirth = dateOfBirth;
        this.turningAge = turningAge;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getOccursOn() { return occursOn; }
    public void setOccursOn(LocalDate occursOn) { this.occursOn = occursOn; }
    public int getTurningAge() { return turningAge; }
    public void setTurningAge(int turningAge) { this.turningAge = turningAge; }
}