package com.hrms.service;

import com.hrms.dto.executive.*;
import com.hrms.enum_status.PayrollMonth;

import java.time.LocalDate;

public interface ExecutiveReportService {
    HeadcountReportDTO getHeadcountReport(LocalDate from, LocalDate to);
    OnboardingReportDTO getOnboardingReport();
    AttendanceReportDTO getAttendanceReport(LocalDate from, LocalDate to);
    LeaveReportDTO getLeaveReport(LocalDate from, LocalDate to);
    AssetReportDTO getAssetReport();
    ExitReportDTO getExitReport(int year);
    ExpenseReportDTO getExpenseReport(LocalDate from, LocalDate to);
    PayrollExecReportDTO getPayrollReport(PayrollMonth month, Integer year);
    ExecutiveReportFullDTO getFullReport(PayrollMonth month, Integer year, LocalDate from, LocalDate to);
}