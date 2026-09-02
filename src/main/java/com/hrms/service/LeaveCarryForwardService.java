package com.hrms.service;

import com.hrms.dto.response.CarryForwardBalanceDTO;
import java.util.List;
import java.util.Map;

public interface LeaveCarryForwardService {

    /**
     * Year-end rollover: for every employee + every leave type whose policy
     * config has carryForwardEnabled=true, computes
     *   carryDays = min(remainingBalance, annualEntitlement * percent/100, maxDays)
     * (all three numbers pulled from LeavePolicyConfigEntity / LeaveRuleEntity —
     * nothing hardcoded) and creates a carry-forward bucket expiring
     * carryForwardExpiryMonths after 1-Jan of the new year.
     *
     * Idempotent — running it twice for the same fromYear does not double-credit,
     * it just updates the existing record.
     *
     * HR/Admin triggers this manually via the controller (or your own scheduler
     * can call it on 1-Jan) — no code change to your existing services needed.
     */
    Map<String, Object> processYearEndCarryForward(int fromYear);

    /**
     * Marks any ACTIVE carry-forward bucket past its expiryDate as LAPSED.
     * Per policy: "Any unused carried-forward leave remaining after this
     * period will automatically lapse." No encashment — lapsed days are
     * simply forfeited, matching "Casual Leave cannot be encashed under
     * any circumstances."
     */
    Map<String, Object> lapseExpiredCarryForwards();

    /**
     * Combined balance for one employee/leaveType/year = fresh allotment
     * (from your existing EmployeeLeaveBalanceEntity) + any still-active,
     * non-expired carried-forward days for that year.
     */
    CarryForwardBalanceDTO getCarryForwardBalance(String empId, String leaveType, int year);

    List<CarryForwardBalanceDTO> getAllCarryForwardBalances(String empId, int year);

    /**
     * Deducts approved leave days from the carry-forward bucket FIRST
     * (since it expires sooner than the fresh allotment), returning how
     * many days are still left to deduct from the fresh balance.
     * Call this from your leave-approval flow before touching the fresh
     * EmployeeLeaveBalanceEntity, if you want carry-forward days consumed
     * in the correct priority order. Purely additive — safe no-op if the
     * employee/leaveType has no active carry-forward bucket.
     */
    double deductFromCarryForwardFirst(String empId, String leaveType, int year, double daysToDeduct);

    /**
     * Restores days to the carry-forward bucket if an approved leave that
     * had consumed carry-forward days gets cancelled/rejected afterwards.
     * Returns how many days still need to be restored to the FRESH balance
     * (i.e. days that had originally overflowed past the carry-forward
     * bucket into fresh balance during deduction) — symmetric with
     * deductFromCarryForwardFirst()'s return value.
     */
    double restoreToCarryForward(String empId, String leaveType, int year, double daysToRestore);

    /**
     * ONE-TIME MIGRATION ENTRY POINT — for when this system is newly
     * deployed and historical (pre-system) leave data doesn't exist to
     * auto-compute from. HR manually enters what each employee's actual
     * carried-forward balance is (from their old records/spreadsheet),
     * bypassing the auto-calculation in processYearEndCarryForward()
     * entirely. Every employee's value is different — that's expected,
     * this endpoint is called once per employee with whatever number HR
     * has on file for them.
     *
     * Still respects policy config for expiry — if expiryDate isn't
     * explicitly provided, it's computed as
     * (1-Jan-of-toYear + carryForwardExpiryMonths) from the leave type's
     * policy config, exactly like the automatic path does. Not exempt
     * from the max-days cap either — if carriedDays exceeds the
     * policy's carryForwardMaxDays, it's silently capped (never trusts
     * manual entry to exceed policy).
     *
     * Going forward (e.g. year-end 2026 -> 2027), the normal automatic
     * processYearEndCarryForward() takes over once the system has its
     * own tracked balance data — this manual entry point is only needed
     * for this one transition year.
     */
    Map<String, Object> manualCarryForwardEntry(String empId, String leaveType, int toYear,
                                                double carriedDays, java.time.LocalDate expiryDate);
}
