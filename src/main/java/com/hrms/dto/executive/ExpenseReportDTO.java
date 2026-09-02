package com.hrms.dto.executive;

import java.util.Map;

// ExpenseReportDTO.java
public class ExpenseReportDTO {
    private Map<String, Double> statusWiseTotal;   // Pending/Approved/Rejected -> sum
    private Map<String, Double> typeWiseTotal;

    public ExpenseReportDTO(Map<String, Double> statusWiseTotal, Map<String, Double> typeWiseTotal) {
        this.statusWiseTotal = statusWiseTotal;
        this.typeWiseTotal = typeWiseTotal;
    }
    public Map<String, Double> getStatusWiseTotal() { return statusWiseTotal; }
    public Map<String, Double> getTypeWiseTotal() { return typeWiseTotal; }
}