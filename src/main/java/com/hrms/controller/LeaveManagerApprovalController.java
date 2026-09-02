package com.hrms.controller;

import com.hrms.dto.request.ManagerApprovalActionDTO;
import com.hrms.dto.request.RegisterManagerApprovalDTO;
import com.hrms.dto.response.LwpEligibilityDTO;
import com.hrms.entity.LeaveManagerApprovalEntity;
import com.hrms.service.LeaveManagerApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────
 *  LWP MANAGER-APPROVAL + ELIGIBILITY API  (new, additive)
 * ─────────────────────────────────────────────────────────────
 *  POST /api/leave/manager-approval/register        → call right after
 *                                                       creating an LWP
 *                                                       leave request
 *  POST /api/leave/manager-approval/{leaveRequestId}/action
 *  GET  /api/leave/manager-approval/{leaveRequestId}/hr-eligible
 *  GET  /api/leave/lwp/eligibility?empId=&year=      → "has this employee
 *                                                       exhausted all paid
 *                                                       leave?"
 *  GET  /api/leave/lwp/payroll-days?empId=&year=&month=  → unpaid days for
 *                                                       feeding payroll LOP
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/leave")
public class LeaveManagerApprovalController {

    @Autowired
    private LeaveManagerApprovalService leaveManagerApprovalService;

    @PostMapping("/manager-approval/register")
    public ResponseEntity<?> register(@RequestBody RegisterManagerApprovalDTO dto) {
        try {
            LeaveManagerApprovalEntity entry = leaveManagerApprovalService.registerForApproval(
                    dto.getLeaveRequestId(), dto.getEmpId(), dto.getLeaveTypeName(),
                    dto.getEmpType(), dto.getDepartment(), dto.isEmergency());
            return ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/manager-approval/{leaveRequestId}/action")
    public ResponseEntity<?> action(@PathVariable Long leaveRequestId,
                                    @RequestBody ManagerApprovalActionDTO dto) {
        try {
            LeaveManagerApprovalEntity entry = leaveManagerApprovalService.managerAction(
                    leaveRequestId, dto.getAction(), dto.getManagerName(), dto.getRemarks());
            return ResponseEntity.ok(entry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/manager-approval/{leaveRequestId}/hr-eligible")
    public ResponseEntity<Map<String, Boolean>> hrEligible(@PathVariable Long leaveRequestId) {
        boolean eligible = leaveManagerApprovalService.isEligibleForHrApproval(leaveRequestId);
        return ResponseEntity.ok(Map.of("hrEligible", eligible));
    }

    @GetMapping("/lwp/eligibility")
    public ResponseEntity<?> lwpEligibility(@RequestParam String empId,
                                            @RequestParam(defaultValue = "0") int year) {
        try {
            int y = year > 0 ? year : LocalDate.now().getYear();
            LwpEligibilityDTO dto = leaveManagerApprovalService.checkLwpEligibility(empId, y);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/lwp/payroll-days")
    public ResponseEntity<?> payrollDays(@RequestParam String empId,
                                         @RequestParam int year,
                                         @RequestParam int month) {
        try {
            double days = leaveManagerApprovalService.calculateUnpaidLeaveDaysForPayroll(
                    empId, YearMonth.of(year, month));
            return ResponseEntity.ok(Map.of("empId", empId, "year", year, "month", month, "unpaidDays", days));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
