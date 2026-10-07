package com.hrms.dto.response;

import java.time.LocalDateTime;

public class CompanyResponseDTO {
    private Long id;
    private String companyName;
    private String gstNumber;
    private String panNumber;
    private String cinNumber;
    private String registrationNumber;
    private String email;
    private String phone;
    private String website;
    private Integer establishedYear;
    private Integer employeeCount;
    private String companyType;
    private String industryType;
    private String address;
    private String description;

    // Store logo URL instead of byte array
    private String logoUrl;
    private String logoContentType;

    private String workingDaysJson;
    private String breakTimingsJson;
    private String holidaysJson;
    private String departmentsJson;
    private String designationsJson;
    private String socialMediaJson;
    private String contactPersonsJson;
    private String bankDetailsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String saturdayPolicyJson;

    public CompanyResponseDTO() {
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse() {
        }

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static ApiResponse success(String message, Object data) {
            return new ApiResponse(true, message, data);
        }

        public static ApiResponse error(String message) {
            return new ApiResponse(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
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

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

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
}
