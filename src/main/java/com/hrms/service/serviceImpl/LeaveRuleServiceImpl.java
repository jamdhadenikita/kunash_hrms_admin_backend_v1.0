package com.hrms.service.serviceImpl;

import com.hrms.entity.LeaveRuleEntity;
import com.hrms.entity.LeaveTypeEntity;
import com.hrms.repository.CompanyRepository;
import com.hrms.repository.LeaveRuleRepository;
import com.hrms.repository.LeaveTypeRepository;
import com.hrms.service.LeaveRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class LeaveRuleServiceImpl implements LeaveRuleService {

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // ─────────────────────────────────────────────────────────────────
    // GET ALL RULES GROUPED by (empType, dept)
    // Returns: [ { empType, dept, allotments: { "Casual Leave": 12 } } ]
    // Same shape as UI's S.rules[] array.
    // Sorted alphabetically by employeeType — no fixed/hardcoded order,
    // since employee types are no longer restricted to a static list.
    // ─────────────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getAllRulesGrouped() {

        List<LeaveRuleEntity> allRules = leaveRuleRepository.findAll();

        allRules.sort(Comparator.comparing(
                r -> r.getEmployeeType() == null ? "" : r.getEmployeeType()));

        Map<String, Map<String, Integer>> grouped = new LinkedHashMap<>();
        for (LeaveRuleEntity r : allRules) {
            String key = r.getEmployeeType() + "||" + r.getDepartment();
            grouped.computeIfAbsent(key, k -> new LinkedHashMap<>())
                    .put(r.getLeaveTypeName(), r.getDaysAllotted());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|\\|");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("empType",    parts[0]);
            row.put("dept",       parts[1]);
            row.put("allotments", entry.getValue());
            result.add(row);
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────
    // Dynamic department list — pulls distinct department names from
    // the company's departments JSON, so it's never a hardcoded list.
    // ─────────────────────────────────────────────────────────────────
    private List<String> getValidDepartments() {
        List<String> result = new ArrayList<>();
        result.add("All");
        try {
            List<String> jsonList = companyRepository.findAllDepartmentsJson();
            ObjectMapper mapper = new ObjectMapper();
            Set<String> seen = new LinkedHashSet<>();

            for (String json : jsonList) {
                if (json == null || json.isBlank()) continue;
                // Each entry is: [{"departmentName":"IT","headOfDepartment":"..."}]
                List<Map<String, Object>> depts = mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                for (Map<String, Object> dept : depts) {
                    Object name = dept.get("departmentName");
                    if (name != null && !name.toString().isBlank()) {
                        seen.add(name.toString().trim());
                    }
                }
            }
            result.addAll(seen);
        } catch (Exception e) {
            // soft fail
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────
    // "Regular" (legacy value already present on existing employee
    // records) and "Full-Time" (the standard label used in the UI going
    // forward) refer to the same employment category for leave purposes.
    // This is a one-off data-label alias, not a policy number — it just
    // prevents existing "Regular" employees from silently showing 0
    // balance once new rules start being created under "Full-Time".
    // ─────────────────────────────────────────────────────────────────
    private String normalizeEmpTypeSynonym(String employeeType) {
        if ("Regular".equalsIgnoreCase(employeeType)) return "Full-Time";
        if ("Full-Time".equalsIgnoreCase(employeeType)) return "Regular";
        return null; // no known synonym
    }

    // ─────────────────────────────────────────────────────────────────
    // SAVE / UPSERT RULE
    //
    // Rules:
    //  1. employeeType just needs to be non-blank — no hardcoded/fixed
    //     list restriction. The UI dropdown already constrains input to
    //     Full-Time / Part-Time / Contract / Intern; the backend doesn't
    //     duplicate that as a rigid enum, so it never goes stale.
    //  2. department must be a valid value (dynamic, from Company data).
    //  3. allotments cannot be null or empty
    //  4. Each day value must be >= 0
    //  5. Loops all active leave types — upserts each row.
    //     If a leave type is missing in the payload it gets 0 days.
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void saveRule(String employeeType, String department, Map<String, Integer> allotments) {

        if (employeeType == null || employeeType.isBlank()) {
            throw new RuntimeException("employeeType is required.");
        }

        List<String> validDepts = getValidDepartments();
        if (!validDepts.contains(department)) {
            throw new RuntimeException("Invalid department: '" + department + "'. Must be one of: " + validDepts);
        }

        if (allotments == null || allotments.isEmpty()) {
            throw new RuntimeException("Allotments map cannot be empty.");
        }

        List<LeaveTypeEntity> activeTypes = leaveTypeRepository.findByIsActiveTrue();
        if (activeTypes.isEmpty()) {
            throw new RuntimeException("No active leave types found. Add leave types first.");
        }

        for (LeaveTypeEntity lt : activeTypes) {
            int days = allotments.getOrDefault(lt.getName(), 0);
            if (days < 0) {
                throw new RuntimeException("Days for '" + lt.getName() + "' cannot be negative.");
            }

            Optional<LeaveRuleEntity> existing = leaveRuleRepository
                    .findByEmployeeTypeAndDepartmentAndLeaveTypeName(employeeType, department, lt.getName());

            if (existing.isPresent()) {
                existing.get().setDaysAllotted(days);
                leaveRuleRepository.save(existing.get());
            } else {
                LeaveRuleEntity rule = new LeaveRuleEntity();
                rule.setEmployeeType(employeeType);
                rule.setDepartment(department);
                rule.setLeaveTypeName(lt.getName());
                rule.setDaysAllotted(days);
                leaveRuleRepository.save(rule);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // DELETE RULE GROUP for (empType, dept)
    // ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteRule(String employeeType, String department) {

        List<LeaveRuleEntity> rules = leaveRuleRepository
                .findByEmployeeTypeAndDepartment(employeeType, department);

        if (rules.isEmpty()) {
            throw new RuntimeException(
                    "No rule found for employeeType='" + employeeType + "' and department='" + department + "'.");
        }
        leaveRuleRepository.deleteAll(rules);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET ALLOTTED DAYS for one (empType, dept, leaveType)
    // Logic: Try specific dept first → fallback to "All" → then retry
    // both with the Regular/Full-Time synonym before giving up.
    // ─────────────────────────────────────────────────────────────────
    @Override
    public int getAllottedDays(String employeeType, String department, String leaveType) {

        // 1. Try exact match
        Optional<LeaveRuleEntity> specific = leaveRuleRepository
                .findByEmployeeTypeAndDepartmentAndLeaveTypeName(employeeType, department, leaveType);
        if (specific.isPresent()) return specific.get().getDaysAllotted();

        // 2. Fallback to "All" department
        Optional<LeaveRuleEntity> fallback = leaveRuleRepository
                .findByEmployeeTypeAndDepartmentAndLeaveTypeName(employeeType, "All", leaveType);
        if (fallback.isPresent()) return fallback.get().getDaysAllotted();

        // 3. Retry with the Regular <-> Full-Time synonym before giving
        // up, so existing "Regular" employees still match rules created
        // under "Full-Time" (and vice versa).
        String synonym = normalizeEmpTypeSynonym(employeeType);
        if (synonym != null) {
            Optional<LeaveRuleEntity> synonymSpecific = leaveRuleRepository
                    .findByEmployeeTypeAndDepartmentAndLeaveTypeName(synonym, department, leaveType);
            if (synonymSpecific.isPresent()) return synonymSpecific.get().getDaysAllotted();

            Optional<LeaveRuleEntity> synonymFallback = leaveRuleRepository
                    .findByEmployeeTypeAndDepartmentAndLeaveTypeName(synonym, "All", leaveType);
            if (synonymFallback.isPresent()) return synonymFallback.get().getDaysAllotted();
        }

        // 4. Log when nothing found — helps debug
        System.out.println("[RULE MISS] No rule for empType=" + employeeType
                + " dept=" + department + " leaveType=" + leaveType);
        return 0;
    }

}