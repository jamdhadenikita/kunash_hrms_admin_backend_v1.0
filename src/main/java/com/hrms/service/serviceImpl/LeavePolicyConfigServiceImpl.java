package com.hrms.service.serviceImpl;

import com.hrms.entity.LeavePolicyConfigEntity;
import com.hrms.repository.LeavePolicyConfigRepository;
import com.hrms.service.LeavePolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LeavePolicyConfigServiceImpl implements LeavePolicyConfigService {

    private static final String FALLBACK_SCOPE = "All";

    @Autowired
    private LeavePolicyConfigRepository repository;

    @Override
    @Transactional
    public LeavePolicyConfigEntity saveOrUpdate(String leaveTypeName, String employeeType, String department,
                                                Boolean carryForwardEnabled, Integer carryForwardPercentOfAnnual,
                                                Integer carryForwardMaxDays, Integer carryForwardExpiryMonths,
                                                Boolean encashmentAllowed, Double compOffCreditDaysPerOccurrence,
                                                Integer compOffExpiryMonths, Boolean requiresManagerApprovalBeforeHr) {

        if (leaveTypeName == null || leaveTypeName.isBlank()) {
            throw new RuntimeException("leaveTypeName is required.");
        }

        String empType = (employeeType != null && !employeeType.isBlank()) ? employeeType.trim() : FALLBACK_SCOPE;
        String dept     = (department != null && !department.isBlank()) ? department.trim() : FALLBACK_SCOPE;

        if (carryForwardPercentOfAnnual != null && (carryForwardPercentOfAnnual < 0 || carryForwardPercentOfAnnual > 100)) {
            throw new RuntimeException("carryForwardPercentOfAnnual must be between 0 and 100.");
        }
        if (carryForwardMaxDays != null && carryForwardMaxDays < 0) {
            throw new RuntimeException("carryForwardMaxDays cannot be negative.");
        }
        if (carryForwardExpiryMonths != null && carryForwardExpiryMonths < 0) {
            throw new RuntimeException("carryForwardExpiryMonths cannot be negative.");
        }
        if (compOffCreditDaysPerOccurrence != null && compOffCreditDaysPerOccurrence < 0) {
            throw new RuntimeException("compOffCreditDaysPerOccurrence cannot be negative.");
        }

        Optional<LeavePolicyConfigEntity> existing =
                repository.findByLeaveTypeNameAndEmployeeTypeAndDepartment(leaveTypeName, empType, dept);

        LeavePolicyConfigEntity entity = existing.orElseGet(LeavePolicyConfigEntity::new);
        entity.setLeaveTypeName(leaveTypeName);
        entity.setEmployeeType(empType);
        entity.setDepartment(dept);
        if (carryForwardEnabled != null) entity.setCarryForwardEnabled(carryForwardEnabled);
        if (carryForwardPercentOfAnnual != null) entity.setCarryForwardPercentOfAnnual(carryForwardPercentOfAnnual);
        if (carryForwardMaxDays != null) entity.setCarryForwardMaxDays(carryForwardMaxDays);
        if (carryForwardExpiryMonths != null) entity.setCarryForwardExpiryMonths(carryForwardExpiryMonths);
        if (encashmentAllowed != null) entity.setEncashmentAllowed(encashmentAllowed);
        if (compOffCreditDaysPerOccurrence != null) entity.setCompOffCreditDaysPerOccurrence(compOffCreditDaysPerOccurrence);
        entity.setCompOffExpiryMonths(compOffExpiryMonths);
        if (requiresManagerApprovalBeforeHr != null) entity.setRequiresManagerApprovalBeforeHr(requiresManagerApprovalBeforeHr);

        return repository.save(entity);
    }

    @Override
    public LeavePolicyConfigEntity resolve(String leaveTypeName, String employeeType, String department) {

        String empType = (employeeType != null && !employeeType.isBlank()) ? employeeType.trim() : FALLBACK_SCOPE;
        String dept     = (department != null && !department.isBlank()) ? department.trim() : FALLBACK_SCOPE;

        // 1. Exact match
        Optional<LeavePolicyConfigEntity> specific =
                repository.findByLeaveTypeNameAndEmployeeTypeAndDepartment(leaveTypeName, empType, dept);
        if (specific.isPresent()) return specific.get();

        // 2. Same employeeType, department = "All"
        Optional<LeavePolicyConfigEntity> deptFallback =
                repository.findByLeaveTypeNameAndEmployeeTypeAndDepartment(leaveTypeName, empType, FALLBACK_SCOPE);
        if (deptFallback.isPresent()) return deptFallback.get();

        // 3. employeeType = "All", department = "All"
        Optional<LeavePolicyConfigEntity> fullFallback =
                repository.findByLeaveTypeNameAndEmployeeTypeAndDepartment(leaveTypeName, FALLBACK_SCOPE, FALLBACK_SCOPE);
        if (fullFallback.isPresent()) return fullFallback.get();

        // 4. Nothing configured -> safe all-disabled default (feature simply off, not an error)
        LeavePolicyConfigEntity none = new LeavePolicyConfigEntity();
        none.setLeaveTypeName(leaveTypeName);
        none.setEmployeeType(empType);
        none.setDepartment(dept);
        none.setCarryForwardEnabled(false);
        none.setCarryForwardPercentOfAnnual(0);
        none.setCarryForwardMaxDays(0);
        none.setCarryForwardExpiryMonths(0);
        none.setEncashmentAllowed(false);
        none.setCompOffCreditDaysPerOccurrence(0.0);
        none.setCompOffExpiryMonths(null);
        none.setRequiresManagerApprovalBeforeHr(false);
        return none;
    }

    @Override
    public List<LeavePolicyConfigEntity> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Policy config not found with id: " + id);
        }
        repository.deleteById(id);
    }
}