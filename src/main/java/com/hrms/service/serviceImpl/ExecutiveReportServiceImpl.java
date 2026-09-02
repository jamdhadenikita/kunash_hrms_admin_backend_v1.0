package com.hrms.service.serviceImpl;

import com.hrms.dto.executive.*;
import com.hrms.enum_status.PayrollMonth;
import com.hrms.repository.*;
import com.hrms.service.ExecutiveReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutiveReportServiceImpl implements ExecutiveReportService {

    private final EmployeeRepository employeeRepository;
    private final OnboardingRepository onboardingRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AssetRepository assetRepository;
    private final ExitRepository exitRepository;
    private final ExpenseRepository expenseRepository;
    private final PayrollRepository payrollRepository;


    @Autowired
    public ExecutiveReportServiceImpl(EmployeeRepository employeeRepository, OnboardingRepository onboardingRepository, AttendanceRepository attendanceRepository,
                                      LeaveRequestRepository leaveRequestRepository,
                                      AssetRepository assetRepository, ExitRepository exitRepository,
                                      ExpenseRepository expenseRepository, PayrollRepository payrollRepository) {
        this.employeeRepository = employeeRepository;
        this.onboardingRepository = onboardingRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.assetRepository = assetRepository;
        this.exitRepository = exitRepository;
        this.expenseRepository = expenseRepository;
        this.payrollRepository = payrollRepository;
    }

    @Override
    public HeadcountReportDTO getHeadcountReport(LocalDate from, LocalDate to) {
        long totalActive = employeeRepository.countByStatus("ACTIVE");

        Map<String, Long> deptWise = new LinkedHashMap<>();
        for (Object[] row : employeeRepository.countActiveByDepartment()) {
            deptWise.put((String) row[0], (Long) row[1]);
        }

        long newJoiners = employeeRepository.findRecentHires(from).stream()
                .filter(e -> !e.getCreatedAt().isAfter(to))
                .count();

        long exitsInRange = 0;
        for (Object[] row : exitRepository.getMonthlyExitCount()) {
            // row[0] = month(int), row[1] = count — only counts current year per existing query
            exitsInRange += (Long) row[1];
        }

        return new HeadcountReportDTO(totalActive, deptWise, newJoiners, exitsInRange);
    }

    @Override
    public OnboardingReportDTO getOnboardingReport() {
        long pending = onboardingRepository.countPendingOnboardings();
        Double avgDays = onboardingRepository.getAvgOnboardingCompletionDays();
        return new OnboardingReportDTO(pending, avgDays);
    }

    @Override
    public AttendanceReportDTO getAttendanceReport(LocalDate from, LocalDate to) {
        long present = attendanceRepository.countPresentInRange(from, to);
        long total = attendanceRepository.countTotalInRange(from, to);
        long wfh = attendanceRepository.countWfhInRange(from, to);
        return new AttendanceReportDTO(present, total, wfh);
    }

    @Override
    public LeaveReportDTO getLeaveReport(LocalDate from, LocalDate to) {
        int approvedDays = leaveRequestRepository.sumApprovedDaysInRange(from, to);
        long pending = leaveRequestRepository.countByStatusAndActionDateBetween("pending", from, to);
        long onLeaveToday = leaveRequestRepository.countEmployeesOnLeaveToday(LocalDate.now());

        Map<String, Long> typeBreakdown = new LinkedHashMap<>();
        for (Object[] row : leaveRequestRepository.countApprovedByLeaveTypeInRange(from, to)) {
            typeBreakdown.put((String) row[0], (Long) row[1]);
        }

        return new LeaveReportDTO(approvedDays, pending, onLeaveToday, typeBreakdown);
    }

    @Override
    public AssetReportDTO getAssetReport() {
        Map<String, Long> statusWise = new LinkedHashMap<>();
        for (Object[] row : assetRepository.getAssetStatusCount()) {
            statusWise.put((String) row[0], (Long) row[1]);
        }
        Map<String, Long> categoryWise = new LinkedHashMap<>();
        for (Object[] row : assetRepository.getAssetCategoryCount()) {
            categoryWise.put((String) row[0], (Long) row[1]);
        }
        long overdue = assetRepository.countOverdueReturns(LocalDate.now());
        return new AssetReportDTO(statusWise, categoryWise, overdue);
    }

    @Override
    public ExitReportDTO getExitReport(int year) {
        long completed = exitRepository.countCompletedExitsThisYear();

        Map<String, Long> deptWise = new LinkedHashMap<>();
        for (Object[] row : exitRepository.getDepartmentWiseExitCount()) {
            deptWise.put((String) row[0], (Long) row[1]);
        }

        Map<String, Long> reasonWise = new LinkedHashMap<>();
        for (Object[] row : exitRepository.getReasonWiseExitCount(year)) {
            reasonWise.put((String) row[0], (Long) row[1]);
        }

        Double avgTenure = exitRepository.getAvgTenureAtExitDays(year);

        return new ExitReportDTO(completed, deptWise, reasonWise, avgTenure);
    }

    @Override
    public ExpenseReportDTO getExpenseReport(LocalDate from, LocalDate to) {
        Map<String, Double> statusWise = new LinkedHashMap<>();
        for (Object[] row : expenseRepository.sumAmountByStatusInRange(from, to)) {
            statusWise.put((String) row[0], (Double) row[1]);
        }
        Map<String, Double> typeWise = new LinkedHashMap<>();
        for (Object[] row : expenseRepository.getExpenseSummaryByType()) {
            typeWise.put((String) row[0], (Double) row[1]);
        }
        return new ExpenseReportDTO(statusWise, typeWise);
    }

    @Override
    public PayrollExecReportDTO getPayrollReport(PayrollMonth month, Integer year) {
        List<Object[]> summary = payrollRepository.getSummaryByMonthAndYear(month, year);
        long totalEmployees = 0;
        double gross = 0, deductions = 0, net = 0;
        if (!summary.isEmpty()) {
            Object[] row = summary.get(0);
            totalEmployees = (Long) row[0];
            gross = (Double) row[1];
            deductions = (Double) row[2];
            net = (Double) row[3];
        }

        Map<String, double[]> deptWise = new LinkedHashMap<>();
        for (Object[] row : payrollRepository.getDeptWiseCostAndCtc(month, year)) {
            deptWise.put((String) row[0], new double[]{(Double) row[1], (Double) row[2]});
        }

        return new PayrollExecReportDTO(totalEmployees, gross, deductions, net, deptWise);
    }

    @Override
    public ExecutiveReportFullDTO getFullReport(PayrollMonth month, Integer year, LocalDate from, LocalDate to) {
        return new ExecutiveReportFullDTO(
                getHeadcountReport(from, to),
                getOnboardingReport(),
                getAttendanceReport(from, to),
                getLeaveReport(from, to),
                getAssetReport(),
                getExitReport(year),
                getExpenseReport(from, to),
                getPayrollReport(month, year)
        );
    }
}