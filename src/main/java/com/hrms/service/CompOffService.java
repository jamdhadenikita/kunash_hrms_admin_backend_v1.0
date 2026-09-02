package com.hrms.service;

import com.hrms.dto.response.CompOffBalanceDTO;
import java.time.LocalDate;
import java.util.Map;

public interface CompOffService {

    /**
     * Credits Comp Off for an employee who worked an approved weekly-off
     * or public holiday. leaveTypeName is passed in (not hardcoded) so it
     * matches whatever name HR gave the Comp-Off type in LeaveTypeEntity
     * (e.g. "Comp Off", "Compensatory Off"). creditedDays/expiry come from
     * LeavePolicyConfigEntity for that leaveTypeName.
     *
     * Also increases the employee's EmployeeLeaveBalanceEntity.allotted for
     * that leave type/year so it's immediately usable through your existing
     * leave-request flow — no change needed to LeaveRequestServiceImpl.
     */
    Map<String, Object> creditCompOff(String empId, String leaveTypeName, LocalDate workedDate,
                                      String reason, String creditedBy);

    CompOffBalanceDTO getAvailableBalance(String empId);

    /** Marks AVAILABLE comp-off entries past their expiry as EXPIRED and reduces the linked balance accordingly. */
    Map<String, Object> expireStaleCompOff(String leaveTypeName);
}