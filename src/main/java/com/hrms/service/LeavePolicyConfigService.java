package com.hrms.service;

import com.hrms.entity.LeavePolicyConfigEntity;
import java.util.List;

public interface LeavePolicyConfigService {

    LeavePolicyConfigEntity saveOrUpdate(String leaveTypeName, String employeeType, String department,
                                         Boolean carryForwardEnabled, Integer carryForwardPercentOfAnnual,
                                         Integer carryForwardMaxDays, Integer carryForwardExpiryMonths,
                                         Boolean encashmentAllowed, Double compOffCreditDaysPerOccurrence,
                                         Integer compOffExpiryMonths, Boolean requiresManagerApprovalBeforeHr);

    /**
     * Resolves the applicable config for (leaveType, employeeType, department),
     * falling back to department="All" then employeeType="All" if no exact
     * match exists — mirrors LeaveRuleServiceImpl.getAllottedDays() fallback logic.
     * Returns a safe all-disabled default if nothing is configured at all
     * (never throws — absence of config just means the feature is off for that type).
     */
    LeavePolicyConfigEntity resolve(String leaveTypeName, String employeeType, String department);

    List<LeavePolicyConfigEntity> getAll();

    void delete(Long id);
}
