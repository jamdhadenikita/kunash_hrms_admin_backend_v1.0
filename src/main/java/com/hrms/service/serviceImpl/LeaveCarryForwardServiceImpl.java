package com.hrms.service.serviceImpl;

import com.hrms.dto.response.CarryForwardBalanceDTO;
import com.hrms.entity.EmployeeEntity;
import com.hrms.entity.EmployeeLeaveBalanceEntity;
import com.hrms.entity.EmployeeLeaveCarryForwardEntity;
import com.hrms.entity.LeavePolicyConfigEntity;
import com.hrms.entity.LeaveTypeEntity;
import com.hrms.repository.EmployeeLeaveBalanceRepository;
import com.hrms.repository.EmployeeLeaveCarryForwardRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeaveCarryForwardService;
import com.hrms.service.LeavePolicyConfigService;
import com.hrms.service.LeaveRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveCarryForwardServiceImpl implements LeaveCarryForwardService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmployeeLeaveBalanceRepository leaveBalanceRepository;
    @Autowired private EmployeeLeaveCarryForwardRepository carryForwardRepository;
    @Autowired private LeaveTypeRepository leaveTypeRepository;
    @Autowired private LeaveRuleService leaveRuleService;
    @Autowired private LeavePolicyConfigService leavePolicyConfigService;

    @Override
    @Transactional
    public Map<String, Object> processYearEndCarryForward(int fromYear) {

        int toYear = fromYear + 1;
        List<LeaveTypeEntity> activeTypes = leaveTypeRepository.findByIsActiveTrue();
        List<EmployeeEntity> employees = employeeRepository.findAll();

        int processedCount = 0;
        int skippedCount = 0;
        List<String> details = new ArrayList<>();

        for (EmployeeEntity emp : employees) {
            String empId = emp.getEmployeeId();
            String empType = (emp.getEmploymentType() != null && !emp.getEmploymentType().isBlank())
                    ? emp.getEmploymentType().trim() : "Full-Time";
            String dept = (emp.getDepartment() != null && !emp.getDepartment().isBlank())
                    ? emp.getDepartment().trim() : "All";

            for (LeaveTypeEntity lt : activeTypes) {

                LeavePolicyConfigEntity policy = leavePolicyConfigService.resolve(lt.getName(), empType, dept);
                if (policy.getCarryForwardEnabled() == null || !policy.getCarryForwardEnabled()) {
                    continue; // carry-forward not enabled for this leave type -> skip, nothing hardcoded
                }

                Optional<EmployeeLeaveBalanceEntity> balanceOpt =
                        leaveBalanceRepository.findByEmpIdAndLeaveTypeAndYear(empId, lt.getName(), fromYear);
                if (balanceOpt.isEmpty()) {
                    skippedCount++;
                    continue; // no activity for this employee/leaveType/year -> nothing to carry
                }

                EmployeeLeaveBalanceEntity balance = balanceOpt.get();
                double remaining = Math.max(0, balance.getRemaining());

                int annualEntitlement = leaveRuleService.getAllottedDays(empType, dept, lt.getName());
                double percentCap = annualEntitlement * (policy.getCarryForwardPercentOfAnnual() / 100.0);
                double hardCap = policy.getCarryForwardMaxDays();

                double carryDays = Math.min(remaining, Math.min(percentCap, hardCap));
                carryDays = Math.floor(carryDays * 100) / 100.0; // avoid floating point noise, no hardcoded rounding rule beyond precision safety

                if (carryDays <= 0) {
                    skippedCount++;
                    continue;
                }

                LocalDate expiry = LocalDate.of(toYear, 1, 1).plusMonths(policy.getCarryForwardExpiryMonths());

                EmployeeLeaveCarryForwardEntity record = carryForwardRepository
                        .findByEmpIdAndLeaveTypeAndFromYear(empId, lt.getName(), fromYear)
                        .orElseGet(EmployeeLeaveCarryForwardEntity::new);

                record.setEmpId(empId);
                record.setLeaveType(lt.getName());
                record.setFromYear(fromYear);
                record.setToYear(toYear);
                record.setCarriedDays(carryDays);
                if (record.getId() == null) record.setUsedFromCarry(0);
                record.setExpiryDate(expiry);
                record.setStatus("ACTIVE");

                carryForwardRepository.save(record);
                processedCount++;
                details.add(empId + " / " + lt.getName() + " -> " + carryDays + " day(s), expires " + expiry);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromYear", fromYear);
        result.put("toYear", toYear);
        result.put("processedCount", processedCount);
        result.put("skippedCount", skippedCount);
        result.put("details", details);
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> lapseExpiredCarryForwards() {

        List<EmployeeLeaveCarryForwardEntity> expired =
                carryForwardRepository.findByStatusAndExpiryDateBefore("ACTIVE", LocalDate.now());

        for (EmployeeLeaveCarryForwardEntity record : expired) {
            record.setStatus("LAPSED");
            carryForwardRepository.save(record);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lapsedCount", expired.size());
        result.put("lapsedRecords", expired.stream()
                .map(r -> r.getEmpId() + "/" + r.getLeaveType() + "/" + r.getFromYear())
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public CarryForwardBalanceDTO getCarryForwardBalance(String empId, String leaveType, int year) {

        List<EmployeeLeaveCarryForwardEntity> records =
                carryForwardRepository.findByEmpIdAndLeaveTypeAndToYear(empId, leaveType, year);

        EmployeeLeaveCarryForwardEntity active = records.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        CarryForwardBalanceDTO dto = new CarryForwardBalanceDTO();
        dto.setEmpId(empId);
        dto.setLeaveType(leaveType);
        dto.setToYear(year);

        if (active == null) {
            dto.setFromYear(year - 1);
            dto.setCarriedDays(0);
            dto.setUsedFromCarry(0);
            dto.setRemainingCarry(0);
            dto.setStatus("NONE");
            return dto;
        }

        // Auto-lapse on read if past expiry but not yet swept by the scheduled/manual job
        if (active.getExpiryDate() != null && active.getExpiryDate().isBefore(LocalDate.now())) {
            active.setStatus("LAPSED");
            carryForwardRepository.save(active);
            dto.setFromYear(active.getFromYear());
            dto.setCarriedDays(active.getCarriedDays());
            dto.setUsedFromCarry(active.getUsedFromCarry());
            dto.setRemainingCarry(0);
            dto.setExpiryDate(active.getExpiryDate());
            dto.setStatus("LAPSED");
            return dto;
        }

        dto.setFromYear(active.getFromYear());
        dto.setCarriedDays(active.getCarriedDays());
        dto.setUsedFromCarry(active.getUsedFromCarry());
        dto.setRemainingCarry(active.getRemainingCarry());
        dto.setExpiryDate(active.getExpiryDate());
        dto.setStatus(active.getRemainingCarry() <= 0 ? "FULLY_USED" : "ACTIVE");
        return dto;
    }

    @Override
    public List<CarryForwardBalanceDTO> getAllCarryForwardBalances(String empId, int year) {
        List<LeaveTypeEntity> activeTypes = leaveTypeRepository.findByIsActiveTrue();
        List<CarryForwardBalanceDTO> result = new ArrayList<>();
        for (LeaveTypeEntity lt : activeTypes) {
            CarryForwardBalanceDTO dto = getCarryForwardBalance(empId, lt.getName(), year);
            if (!"NONE".equals(dto.getStatus())) {
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public double deductFromCarryForwardFirst(String empId, String leaveType, int year, double daysToDeduct) {

        List<EmployeeLeaveCarryForwardEntity> records =
                carryForwardRepository.findByEmpIdAndLeaveTypeAndToYear(empId, leaveType, year);

        Optional<EmployeeLeaveCarryForwardEntity> activeOpt = records.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .filter(r -> r.getExpiryDate() == null || !r.getExpiryDate().isBefore(LocalDate.now()))
                .findFirst();

        if (activeOpt.isEmpty()) {
            return daysToDeduct; // nothing to draw from -> deduct fully from fresh balance
        }

        EmployeeLeaveCarryForwardEntity record = activeOpt.get();
        double available = record.getRemainingCarry();
        double drawFromCarry = Math.min(available, daysToDeduct);

        record.setUsedFromCarry(record.getUsedFromCarry() + drawFromCarry);
        if (record.getRemainingCarry() <= 0) {
            record.setStatus("FULLY_USED");
        }
        carryForwardRepository.save(record);

        return daysToDeduct - drawFromCarry; // remainder still owed to fresh balance
    }

    @Override
    @Transactional
    public double restoreToCarryForward(String empId, String leaveType, int year, double daysToRestore) {

        List<EmployeeLeaveCarryForwardEntity> records =
                carryForwardRepository.findByEmpIdAndLeaveTypeAndToYear(empId, leaveType, year);

        Optional<EmployeeLeaveCarryForwardEntity> recordOpt = records.stream().findFirst();
        if (recordOpt.isEmpty()) return daysToRestore; // no carry bucket at all -> everything goes to fresh

        EmployeeLeaveCarryForwardEntity record = recordOpt.get();
        double restorableToCarry = Math.min(record.getUsedFromCarry(), daysToRestore);

        double newUsed = record.getUsedFromCarry() - restorableToCarry;
        record.setUsedFromCarry(newUsed);
        if ("FULLY_USED".equals(record.getStatus()) && record.getRemainingCarry() > 0) {
            record.setStatus("ACTIVE");
        }
        carryForwardRepository.save(record);

        return daysToRestore - restorableToCarry; // remainder still owed to fresh balance
    }

    @Override
    @Transactional
    public Map<String, Object> manualCarryForwardEntry(String empId, String leaveType, int toYear,
                                                       double carriedDays, LocalDate expiryDate) {

        if (empId == null || empId.isBlank()) throw new RuntimeException("empId is required.");
        if (leaveType == null || leaveType.isBlank()) throw new RuntimeException("leaveType is required.");
        if (carriedDays <= 0) throw new RuntimeException("carriedDays must be greater than 0.");

        EmployeeEntity emp = employeeRepository.findByEmployeeId(empId)
                .orElseThrow(() -> new RuntimeException("Employee '" + empId + "' not found."));

        String empType = (emp.getEmploymentType() != null && !emp.getEmploymentType().isBlank())
                ? emp.getEmploymentType().trim() : "Full-Time";
        String dept = (emp.getDepartment() != null && !emp.getDepartment().isBlank())
                ? emp.getDepartment().trim() : "All";

        LeavePolicyConfigEntity policy = leavePolicyConfigService.resolve(leaveType, empType, dept);

        // Still respects the policy's max-days cap — manual entry can't
        // silently exceed what the policy allows, even during migration.
        double effectiveCarriedDays = carriedDays;
        if (policy.getCarryForwardMaxDays() != null && policy.getCarryForwardMaxDays() > 0
                && carriedDays > policy.getCarryForwardMaxDays()) {
            effectiveCarriedDays = policy.getCarryForwardMaxDays();
        }

        LocalDate effectiveExpiry = expiryDate;
        if (effectiveExpiry == null) {
            int expiryMonths = (policy.getCarryForwardExpiryMonths() != null) ? policy.getCarryForwardExpiryMonths() : 0;
            effectiveExpiry = LocalDate.of(toYear, 1, 1).plusMonths(expiryMonths);
        }

        int fromYear = toYear - 1;

        EmployeeLeaveCarryForwardEntity record = carryForwardRepository
                .findByEmpIdAndLeaveTypeAndFromYear(empId, leaveType, fromYear)
                .orElseGet(EmployeeLeaveCarryForwardEntity::new);

        record.setEmpId(empId);
        record.setLeaveType(leaveType);
        record.setFromYear(fromYear);
        record.setToYear(toYear);
        record.setCarriedDays(effectiveCarriedDays);
        if (record.getId() == null) record.setUsedFromCarry(0);
        record.setExpiryDate(effectiveExpiry);
        record.setStatus("ACTIVE");

        carryForwardRepository.save(record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("empId", empId);
        result.put("leaveType", leaveType);
        result.put("toYear", toYear);
        result.put("carriedDays", effectiveCarriedDays);
        result.put("expiryDate", effectiveExpiry);
        result.put("cappedByPolicy", effectiveCarriedDays < carriedDays);
        result.put("message", "Manual carry-forward entry saved successfully.");
        return result;
    }
}
