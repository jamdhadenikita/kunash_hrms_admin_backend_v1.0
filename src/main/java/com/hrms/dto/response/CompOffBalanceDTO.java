package com.hrms.dto.response;

public class CompOffBalanceDTO {

    private String empId;
    private double totalAvailableDays;
    private java.util.List<CompOffEntryDTO> entries;

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }
    public double getTotalAvailableDays() { return totalAvailableDays; }
    public void setTotalAvailableDays(double totalAvailableDays) { this.totalAvailableDays = totalAvailableDays; }
    public java.util.List<CompOffEntryDTO> getEntries() { return entries; }
    public void setEntries(java.util.List<CompOffEntryDTO> entries) { this.entries = entries; }

    public static class CompOffEntryDTO {
        private java.time.LocalDate workedDate;
        private double creditedDays;
        private java.time.LocalDate expiryDate;
        private String status;

        public java.time.LocalDate getWorkedDate() { return workedDate; }
        public void setWorkedDate(java.time.LocalDate workedDate) { this.workedDate = workedDate; }
        public double getCreditedDays() { return creditedDays; }
        public void setCreditedDays(double creditedDays) { this.creditedDays = creditedDays; }
        public java.time.LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(java.time.LocalDate expiryDate) { this.expiryDate = expiryDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}