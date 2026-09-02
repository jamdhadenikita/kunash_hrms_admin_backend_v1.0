package com.hrms.service.serviceImpl;

import com.hrms.dto.response.CompOffBalanceDTO;
import com.hrms.entity.CompOffEarnedEntity;
import com.hrms.entity.EmployeeEntity;
import com.hrms.entity.EmployeeLeaveBalanceEntity;
import com.hrms.entity.LeavePolicyConfigEntity;
import com.hrms.repository.CompOffEarnedRepository;
import com.hrms.repository.EmployeeLeaveBalanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.CompOffService;
import com.hrms.service.LeavePolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class CompOffServiceImpl implements CompOffService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private CompOffEarnedRepository compOffRepository;
    @Autowired private EmployeeLeaveBalanceRepository leaveBalanceRepository;
    @Autowired private LeavePolicyConfigService leavePolicyConfigService;

    @Override
    @Transactional
    public Map<String, Object> creditCompOff(String empId, String leaveTypeName, LocalDate workedDate,
                                             String reason, String creditedBy) {

        if (empId == null || empId.isBlank()) throw new RuntimeException("empId is required.");
        if (leaveTypeName == null || leaveTypeName.isBlank()) throw new RuntimeException("leaveTypeName is required.");
        if (workedDate == null) throw new RuntimeException("workedDate is required.");

        EmployeeEntity emp = employeeRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new RuntimeException("Employee '" + empId + "' not found."));

        if (compOffRepository.findByEmpIdAndWorkedDate(empId, workedDate).isPresent()) {
            throw new RuntimeException("Comp Off already credited for " + empId + " on " + workedDate + ".");
        }

        String empType = (emp.getEmploymentType() != null && !emp.getEmploymentType().isBlank())
                ? emp.getEmploymentType().trim() : "Full-Time";
        String dept = (emp.getDepartment() != null && !emp.getDepartment().isBlank())
                ? emp.getDepartment().trim() : "All";

        LeavePolicyConfigEntity policy = leavePolicyConfigService.resolve(leaveTypeName, empType, dept);
        double creditDays = policy.getCompOffCreditDaysPerOccurrence() != null
                ? policy.getCompOffCreditDaysPerOccurrence() : 0.0;

        if (creditDays <= 0) {
            throw new RuntimeException(
                    "No compOffCreditDaysPerOccurrence configured for leave type '" + leaveTypeName +
                            "' (empType=" + empType + ", dept=" + dept + "). Set it via /api/leave/policy-config first.");
        }

        LocalDate expiry = (policy.getCompOffExpiryMonths() != null && policy.getCompOffExpiryMonths() > 0)
                ? workedDate.plusMonths(policy.getCompOffExpiryMonths())
                : null;

        CompOffEarnedEntity entry = new CompOffEarnedEntity();
        entry.setEmpId(empId);
        entry.setLeaveTypeName(leaveTypeName);
        entry.setWorkedDate(workedDate);
        entry.setReason(reason);
        entry.setCreditedDays(creditDays);
        entry.setExpiryDate(expiry);
        entry.setStatus("AVAILABLE");
        entry.setCreditedBy(creditedBy != null && !creditedBy.isBlank() ? creditedBy : "HR Admin");
        compOffRepository.save(entry);

        // Reflect immediately in the standard balance table so it's usable
        // through the existing leave-request flow with zero changes there.
        int year = workedDate.getYear();
        EmployeeLeaveBalanceEntity balance = leaveBalanceRepository
                .findByEmpIdAndLeaveTypeAndYear(empId, leaveTypeName, year)
                .orElseGet(() -> {
                    EmployeeLeaveBalanceEntity b = new EmployeeLeaveBalanceEntity();
                    b.setEmpId(empId);
                    b.setLeaveType(leaveTypeName);
                    b.setYear(year);
                    b.setAllotted(0);
                    b.setUsed(0);
                    return b;
                });
        balance.setAllotted((int) Math.round(balance.getAllotted() + creditDays));
        leaveBalanceRepository.save(balance);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("empId", empId);
        result.put("leaveType", leaveTypeName);
        result.put("workedDate", workedDate);
        result.put("creditedDays", creditDays);
        result.put("expiryDate", expiry);
        result.put("message", "Comp Off credited successfully.");
        return result;
    }

    @Override
    @Transactional
    public CompOffBalanceDTO getAvailableBalance(String empId) {

        List<CompOffEarnedEntity> entries = compOffRepository.findByEmpIdAndStatus(empId, "AVAILABLE");

        double total = 0;
        List<CompOffBalanceDTO.CompOffEntryDTO> entryDTOs = new ArrayList<>();
        for (CompOffEarnedEntity e : entries) {
            boolean expired = e.getExpiryDate() != null && e.getExpiryDate().isBefore(LocalDate.now());

            if (expired) {
                // Lazy expiry: persist it right here instead of waiting on a
                // scheduled sweep — correctness never depends on a scheduler
                // running. Mirrors the same pattern already used for
                // carry-forward buckets in LeaveCarryForwardServiceImpl.
                e.setStatus("EXPIRED");
                compOffRepository.save(e);

                int year = e.getExpiryDate().getYear();
                leaveBalanceRepository.findByEmpIdAndLeaveTypeAndYear(e.getEmpId(), e.getLeaveTypeName(), year)
                        .ifPresent(balance -> {
                            balance.setAllotted(Math.max(0, (int) (balance.getAllotted() - e.getCreditedDays())));
                            leaveBalanceRepository.save(balance);
                        });
            } else {
                total += e.getCreditedDays();
            }

            CompOffBalanceDTO.CompOffEntryDTO dto = new CompOffBalanceDTO.CompOffEntryDTO();
            dto.setWorkedDate(e.getWorkedDate());
            dto.setCreditedDays(e.getCreditedDays());
            dto.setExpiryDate(e.getExpiryDate());
            dto.setStatus(expired ? "EXPIRED" : "AVAILABLE");
            entryDTOs.add(dto);
        }

        CompOffBalanceDTO result = new CompOffBalanceDTO();
        result.setEmpId(empId);
        result.setTotalAvailableDays(total);
        result.setEntries(entryDTOs);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> expireStaleCompOff(String leaveTypeName) {

        // Optional manual utility — kept for HR's convenience, but no longer
        // required for correctness since getAvailableBalance() now expires
        // stale entries lazily on every read. leaveTypeName here is used only
        // to filter which entries to sweep in one call; if omitted (null or
        // blank), sweeps stale entries for every leave type at once.
        List<CompOffEarnedEntity> stale = compOffRepository.findByStatusAndExpiryDateBefore("AVAILABLE", LocalDate.now());
        if (leaveTypeName != null && !leaveTypeName.isBlank()) {
            stale = stale.stream()
                    .filter(e -> leaveTypeName.equalsIgnoreCase(e.getLeaveTypeName()))
                    .collect(java.util.stream.Collectors.toList());
        }

        int expiredCount = 0;
        for (CompOffEarnedEntity e : stale) {
            e.setStatus("EXPIRED");
            compOffRepository.save(e);

            int year = e.getExpiryDate().getYear();
            leaveBalanceRepository.findByEmpIdAndLeaveTypeAndYear(e.getEmpId(), e.getLeaveTypeName(), year)
                    .ifPresent(balance -> {
                        balance.setAllotted(Math.max(0, (int) (balance.getAllotted() - e.getCreditedDays())));
                        leaveBalanceRepository.save(balance);
                    });
            expiredCount++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expiredCount", expiredCount);
        return result;
    }
}
