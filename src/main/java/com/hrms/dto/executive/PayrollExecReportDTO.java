package com.hrms.dto.executive;

import java.util.Map;

// PayrollExecReportDTO.java
public class PayrollExecReportDTO {
    private long totalEmployees;
    private double totalGross;
    private double totalDeductions;
    private double totalNetPayable;
    private Map<String, double[]> departmentWise; // dept -> [netSalary, ctc]

    public PayrollExecReportDTO(long totalEmployees, double totalGross, double totalDeductions,
                                double totalNetPayable, Map<String, double[]> departmentWise) {
        this.totalEmployees = totalEmployees;
        this.totalGross = totalGross;
        this.totalDeductions = totalDeductions;
        this.totalNetPayable = totalNetPayable;
        this.departmentWise = departmentWise;
    }
    public long getTotalEmployees() { return totalEmployees; }
    public double getTotalGross() { return totalGross; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getTotalNetPayable() { return totalNetPayable; }
    public Map<String, double[]> getDepartmentWise() { return departmentWise; }
}
