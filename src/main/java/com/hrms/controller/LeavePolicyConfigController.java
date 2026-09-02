package com.hrms.controller;

import com.hrms.dto.request.LeavePolicyConfigRequestDTO;
import com.hrms.dto.response.LeavePolicyConfigResponseDTO;
import com.hrms.entity.LeavePolicyConfigEntity;
import com.hrms.service.LeavePolicyConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ─────────────────────────────────────────────────────────────
 *  LEAVE POLICY CONFIG API  (new, additive — does not touch any
 *  existing controller)
 * ─────────────────────────────────────────────────────────────
 *  GET    /api/leave/policy-config                → list all configs
 *  POST   /api/leave/policy-config                 → create/update
 *  DELETE /api/leave/policy-config/{id}            → remove a config row
 *
 *  This is where HR enters the actual policy numbers (carry-forward %,
 *  max days, expiry months, comp-off credit days, etc). Nothing is
 *  hardcoded in the backend — every leave type can have its own rule
 *  set, per employee type and department if needed.
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/leave/policy-config")
public class LeavePolicyConfigController {

    @Autowired
    private LeavePolicyConfigService leavePolicyConfigService;

    @GetMapping
    public ResponseEntity<List<LeavePolicyConfigResponseDTO>> getAll() {
        List<LeavePolicyConfigResponseDTO> result = leavePolicyConfigService.getAll()
                .stream().map(LeavePolicyConfigResponseDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> saveOrUpdate(@RequestBody LeavePolicyConfigRequestDTO dto) {
        try {
            LeavePolicyConfigEntity saved = leavePolicyConfigService.saveOrUpdate(
                    dto.getLeaveTypeName(), dto.getEmployeeType(), dto.getDepartment(),
                    dto.getCarryForwardEnabled(), dto.getCarryForwardPercentOfAnnual(),
                    dto.getCarryForwardMaxDays(), dto.getCarryForwardExpiryMonths(),
                    dto.getEncashmentAllowed(), dto.getCompOffCreditDaysPerOccurrence(),
                    dto.getCompOffExpiryMonths(), dto.getRequiresManagerApprovalBeforeHr());
            return ResponseEntity.ok(LeavePolicyConfigResponseDTO.from(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            leavePolicyConfigService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Policy config deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

