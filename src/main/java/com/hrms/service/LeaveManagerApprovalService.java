package com.hrms.service;

import com.hrms.dto.response.LwpEligibilityDTO;
import com.hrms.entity.LeaveManagerApprovalEntity;

import java.time.YearMonth;

public interface LeaveManagerApprovalService {

    /**
     * Call this right after creating a LeaveRequestEntity for a leave type
     * whose policy config has requiresManagerApprovalBeforeHr=true.
     * If isEmergency=true, skips straight to NOT_REQUIRED (policy carve-out
     * for "unforeseen emergencies"), otherwise leaves it PENDING for the
     * manager. If the policy doesn't require manager approval for this
     * leave type at all, also short-circuits to NOT_REQUIRED.
     *
     * This is a pure additive call — your existing createRequest() method
     * doesn't need to change unless you want manager-gating enforced;
     * it can also be called independently right after the fact.
     */
    LeaveManagerApprovalEntity registerForApproval(Long leaveRequestId, String empId,
                                                   String leaveTypeName, String empType,
                                                   String department, boolean isEmergency);

    LeaveManagerApprovalEntity managerAction(Long leaveRequestId, String action,
                                             String managerName, String remarks);

    /** True if HR is clear to approve: emergency, not-required, or manager already approved. */
    boolean isEligibleForHrApproval(Long leaveRequestId);

    /**
     * Policy: "LWP may be granted... if an employee has exhausted all
     * available leave balances." Checks whether every PAID leave type's
     * remaining balance is 0 for the given employee/year.
     */
    LwpEligibilityDTO checkLwpEligibility(String empId, int year);

    /**
     * Sums approved-and-unpaid (isPaid=false on LeaveTypeEntity) leave
     * days overlapping the given month, for feeding into
     * PayrollEntity.lopDays / lopDeduction. Pure calculation — does not
     * write to payroll; call this from PayrollComputationService when
     * you're ready to wire it in.
     */
    double calculateUnpaidLeaveDaysForPayroll(String empId, YearMonth month);
}
