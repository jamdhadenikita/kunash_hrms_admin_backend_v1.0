package com.hrms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Table(name = "companies")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(length = 50)
    private String gstNumber;

    @Column(length = 50)
    private String panNumber;

    @Column(length = 50)
    private String cinNumber;

    @Column(length = 50)
    private String registrationNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 200)
    private String website;

    private Integer establishedYear;
    private Integer employeeCount;

    @Column(length = 50)
    private String companyType;

    @Column(length = 100)
    private String industryType;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    private byte[] logo;

    private String logoContentType;

    @Column(columnDefinition = "TEXT")
    private String workingDaysJson;

    @Column(columnDefinition = "TEXT")
    private String breakTimingsJson;

    @Column(columnDefinition = "LONGTEXT")
    private String holidaysJson;

    @Column(columnDefinition = "LONGTEXT")
    private String departmentsJson;

    @Column(columnDefinition = "LONGTEXT")
    private String designationsJson;

    @Column(columnDefinition = "TEXT")
    private String socialMediaJson;

    @Column(columnDefinition = "TEXT")
    private String contactPersonsJson;

    @Column(columnDefinition = "TEXT")
    private String bankDetailsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String saturdayPolicyJson;

    public CompanyEntity() {
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getCinNumber() { return cinNumber; }
    public void setCinNumber(String cinNumber) { this.cinNumber = cinNumber; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public Integer getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(Integer establishedYear) { this.establishedYear = establishedYear; }

    public Integer getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(Integer employeeCount) { this.employeeCount = employeeCount; }

    public String getCompanyType() { return companyType; }
    public void setCompanyType(String companyType) { this.companyType = companyType; }

    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }

    public String getLogoContentType() { return logoContentType; }
    public void setLogoContentType(String logoContentType) { this.logoContentType = logoContentType; }

    public String getWorkingDaysJson() { return workingDaysJson; }
    public void setWorkingDaysJson(String workingDaysJson) { this.workingDaysJson = workingDaysJson; }

    public String getBreakTimingsJson() { return breakTimingsJson; }
    public void setBreakTimingsJson(String breakTimingsJson) { this.breakTimingsJson = breakTimingsJson; }

    public String getHolidaysJson() { return holidaysJson; }
    public void setHolidaysJson(String holidaysJson) { this.holidaysJson = holidaysJson; }

    public String getDepartmentsJson() { return departmentsJson; }
    public void setDepartmentsJson(String departmentsJson) { this.departmentsJson = departmentsJson; }

    public String getDesignationsJson() { return designationsJson; }
    public void setDesignationsJson(String designationsJson) { this.designationsJson = designationsJson; }

    public String getSocialMediaJson() { return socialMediaJson; }
    public void setSocialMediaJson(String socialMediaJson) { this.socialMediaJson = socialMediaJson; }

    public String getContactPersonsJson() { return contactPersonsJson; }
    public void setContactPersonsJson(String contactPersonsJson) { this.contactPersonsJson = contactPersonsJson; }

    public String getBankDetailsJson() { return bankDetailsJson; }
    public void setBankDetailsJson(String bankDetailsJson) { this.bankDetailsJson = bankDetailsJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSaturdayPolicyJson() { return saturdayPolicyJson; }
    public void setSaturdayPolicyJson(String saturdayPolicyJson) { this.saturdayPolicyJson = saturdayPolicyJson; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


