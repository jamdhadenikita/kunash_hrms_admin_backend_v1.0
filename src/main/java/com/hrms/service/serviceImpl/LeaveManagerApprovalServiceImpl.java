package com.hrms.service.serviceImpl;

import com.hrms.dto.response.LwpEligibilityDTO;
import com.hrms.entity.EmployeeLeaveBalanceEntity;
import com.hrms.entity.LeaveManagerApprovalEntity;
import com.hrms.entity.LeavePolicyConfigEntity;
import com.hrms.entity.LeaveRequestEntity;
import com.hrms.entity.LeaveTypeEntity;
import com.hrms.repository.EmployeeLeaveBalanceRepository;
import com.hrms.repository.LeaveManagerApprovalRepository;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeaveManagerApprovalService;
import com.hrms.service.LeavePolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveManagerApprovalServiceImpl implements LeaveManagerApprovalService {

    @Autowired private LeaveManagerApprovalRepository approvalRepository;
    @Autowired private LeavePolicyConfigService leavePolicyConfigService;
    @Autowired private LeaveTypeRepository leaveTypeRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private EmployeeLeaveBalanceRepository leaveBalanceRepository;

    @Override
    @Transactional
    public LeaveManagerApprovalEntity registerForApproval(Long leaveRequestId, String empId,
                                                          String leaveTypeName, String empType,
                                                          String department, boolean isEmergency) {

        LeavePolicyConfigEntity policy = leavePolicyConfigService.resolve(leaveTypeName, empType, department);
        boolean required = policy.getRequiresManagerApprovalBeforeHr() != null
                && policy.getRequiresManagerApprovalBeforeHr();

        LeaveManagerApprovalEntity entry = approvalRepository.findByLeaveRequestId(leaveRequestId)
                .orElseGet(LeaveManagerApprovalEntity::new);

        entry.setLeaveRequestId(leaveRequestId);
        entry.setEmpId(empId);
        entry.setIsEmergency(isEmergency);

        if (!required || isEmergency) {
            entry.setManagerApprovalStatus("NOT_REQUIRED");
        } else {
            entry.setManagerApprovalStatus("PENDING");
        }

        return approvalRepository.save(entry);
    }

    @Override
    @Transactional
    public LeaveManagerApprovalEntity managerAction(Long leaveRequestId, String action,
                                                    String managerName, String remarks) {

        LeaveManagerApprovalEntity entry = approvalRepository.findByLeaveRequestId(leaveRequestId)
                .orElseThrow(() -> new RuntimeException(
                        "No manager-approval record found for leave request " + leaveRequestId +
                                ". It may not require manager approval."));

        if (!"PENDING".equals(entry.getManagerApprovalStatus())) {
            throw new RuntimeException("This request is already '" + entry.getManagerApprovalStatus() + "'.");
        }

        if (!"approve".equalsIgnoreCase(action) && !"reject".equalsIgnoreCase(action)) {
            throw new RuntimeException("Invalid action '" + action + "'. Use 'approve' or 'reject'.");
        }

        entry.setManagerApprovalStatus("approve".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED");
        entry.setManagerName(managerName);
        entry.setManagerActionDate(LocalDate.now());
        entry.setManagerRemarks(remarks);

        return approvalRepository.save(entry);
    }

    @Override
    public boolean isEligibleForHrApproval(Long leaveRequestId) {
        Optional<LeaveManagerApprovalEntity> entryOpt = approvalRepository.findByLeaveRequestId(leaveRequestId);
        if (entryOpt.isEmpty()) {
            return true; // no manager-gate registered for this request -> not blocked by this module
        }
        String status = entryOpt.get().getManagerApprovalStatus();
        return "NOT_REQUIRED".equals(status) || "APPROVED".equals(status)
                || (entryOpt.get().getIsEmergency() != null && entryOpt.get().getIsEmergency());
    }

    @Override
    public LwpEligibilityDTO checkLwpEligibility(String empId, int year) {

        List<LeaveTypeEntity> paidTypes = leaveTypeRepository.findByIsActiveTrue()
                .stream().filter(LeaveTypeEntity::getIsPaid).toList();

        double totalRemaining = 0;
        for (LeaveTypeEntity lt : paidTypes) {
            Optional<EmployeeLeaveBalanceEntity> balanceOpt =
                    leaveBalanceRepository.findByEmpIdAndLeaveTypeAndYear(empId, lt.getName(), year);
            if (balanceOpt.isPresent()) {
                totalRemaining += Math.max(0, balanceOpt.get().getRemaining());
            }
        }

        LwpEligibilityDTO dto = new LwpEligibilityDTO();
        dto.setEmpId(empId);
        dto.setYear(year);
        dto.setTotalRemainingPaidLeave(totalRemaining);
        dto.setEligible(totalRemaining <= 0);
        dto.setMessage(totalRemaining <= 0
                ? "All paid leave balances are exhausted — LWP may be granted at Management's discretion."
                : "Employee still has " + totalRemaining + " day(s) of paid leave remaining. " +
                "Per policy, LWP applies only once all leave balances are exhausted.");
        return dto;
    }

    @Override
    public double calculateUnpaidLeaveDaysForPayroll(String empId, YearMonth month) {

        List<LeaveTypeEntity> unpaidTypes = leaveTypeRepository.findByIsActiveTrue()
                .stream().filter(lt -> !lt.getIsPaid()).toList();

        if (unpaidTypes.isEmpty()) return 0;

        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        List<LeaveRequestEntity> approvedRequests =
                leaveRequestRepository.findByEmpIdAndStatusIn(empId, List.of("approved"));

        double totalDays = 0;
        for (LeaveRequestEntity req : approvedRequests) {

            boolean isUnpaidType = unpaidTypes.stream()
                    .anyMatch(lt -> lt.getName().equalsIgnoreCase(req.getLeaveType()));
            if (!isUnpaidType) continue;

            LocalDate from = req.getFromDate();
            LocalDate to = (req.getToDate() != null) ? req.getToDate()
                    : from.plusDays(req.getNumberOfDays() - 1);

            LocalDate overlapStart = from.isAfter(monthStart) ? from : monthStart;
            LocalDate overlapEnd = to.isBefore(monthEnd) ? to : monthEnd;

            if (!overlapStart.isAfter(overlapEnd)) {
                totalDays += java.time.temporal.ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
            }
        }

        return totalDays;
    }
}