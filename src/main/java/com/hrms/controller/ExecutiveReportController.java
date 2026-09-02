package com.hrms.controller;

import com.hrms.dto.executive.AttendanceReportDTO;
import com.hrms.dto.executive.HeadcountReportDTO;
import com.hrms.dto.executive.OnboardingReportDTO;
import com.hrms.dto.executive.*;
import com.hrms.enum_status.PayrollMonth;
import com.hrms.service.ExecutiveReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/executive-report")
public class ExecutiveReportController {

    @Autowired
    private ExecutiveReportService executiveReportService;

    // GET /api/executive-report/headcount?from=2026-08-01&to=2026-08-31
    @GetMapping("/headcount")
    public HeadcountReportDTO getHeadcount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return executiveReportService.getHeadcountReport(from, to);
    }

    // GET /api/executive-report/onboarding
    @GetMapping("/onboarding")
    public OnboardingReportDTO getOnboarding() {
        return executiveReportService.getOnboardingReport();
    }

    // GET /api/executive-report/attendance?from=...&to=...
    @GetMapping("/attendance")
    public AttendanceReportDTO getAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return executiveReportService.getAttendanceReport(from, to);
    }

    // GET /api/executive-report/leave?from=...&to=...
    @GetMapping("/leave")
    public LeaveReportDTO getLeave(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return executiveReportService.getLeaveReport(from, to);
    }

    // GET /api/executive-report/assets
    @GetMapping("/assets")
    public AssetReportDTO getAssets() {
        return executiveReportService.getAssetReport();
    }

    // GET /api/executive-report/exit?year=2026
    @GetMapping("/exit")
    public ExitReportDTO getExit(@RequestParam int year) {
        return executiveReportService.getExitReport(year);
    }

    // GET /api/executive-report/expense?from=...&to=...
    @GetMapping("/expense")
    public ExpenseReportDTO getExpense(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return executiveReportService.getExpenseReport(from, to);
    }

    // GET /api/executive-report/payroll?month=AUGUST&year=2026
    @GetMapping("/payroll")
    public PayrollExecReportDTO getPayroll(
            @RequestParam PayrollMonth month,
            @RequestParam Integer year) {
        return executiveReportService.getPayrollReport(month, year);
    }

    // GET /api/executive-report/full?month=AUGUST&year=2026&from=...&to=...
    // Combined payload for the single-page report view (avoids 8 round trips on load)
    @GetMapping("/full")
    public ExecutiveReportFullDTO getFullReport(
            @RequestParam PayrollMonth month,
            @RequestParam Integer year,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return executiveReportService.getFullReport(month, year, from, to);
    }
}