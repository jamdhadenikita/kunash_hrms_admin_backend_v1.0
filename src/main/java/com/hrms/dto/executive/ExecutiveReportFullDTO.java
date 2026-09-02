package com.hrms.dto.executive;


// ExecutiveReportFullDTO.java
public class ExecutiveReportFullDTO {
    private HeadcountReportDTO headcount;
    private OnboardingReportDTO onboarding;
    private AttendanceReportDTO attendance;
    private LeaveReportDTO leave;
    private AssetReportDTO asset;
    private ExitReportDTO exit;
    private ExpenseReportDTO expense;
    private PayrollExecReportDTO payroll;

    public ExecutiveReportFullDTO(HeadcountReportDTO headcount, OnboardingReportDTO onboarding,
                                  AttendanceReportDTO attendance, LeaveReportDTO leave, AssetReportDTO asset,
                                  ExitReportDTO exit, ExpenseReportDTO expense, PayrollExecReportDTO payroll) {
        this.headcount = headcount; this.onboarding = onboarding; this.attendance = attendance;
        this.leave = leave; this.asset = asset; this.exit = exit; this.expense = expense; this.payroll = payroll;
    }
    // getters
    public HeadcountReportDTO getHeadcount() { return headcount; }
    public OnboardingReportDTO getOnboarding() { return onboarding; }
    public AttendanceReportDTO getAttendance() { return attendance; }
    public LeaveReportDTO getLeave() { return leave; }
    public AssetReportDTO getAsset() { return asset; }
    public ExitReportDTO getExit() { return exit; }
    public ExpenseReportDTO getExpense() { return expense; }
    public PayrollExecReportDTO getPayroll() { return payroll; }
}