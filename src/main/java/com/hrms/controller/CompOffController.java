package com.hrms.controller;

import com.hrms.dto.request.CompOffCreditRequestDTO;
import com.hrms.dto.response.CompOffBalanceDTO;
import com.hrms.service.CompOffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────
 *  COMP OFF API  (new, additive)
 * ─────────────────────────────────────────────────────────────
 *  POST /api/leave/comp-off/credit                 → HR marks a worked
 *                                                      weekly-off/holiday
 *  GET  /api/leave/comp-off/balance?empId=EMP001    → available balance
 *  POST /api/leave/comp-off/expire-stale?leaveTypeName=Comp Off
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/leave/comp-off")
public class CompOffController {

    @Autowired
    private CompOffService compOffService;

    @PostMapping("/credit")
    public ResponseEntity<?> credit(@RequestBody CompOffCreditRequestDTO dto) {
        try {
            Map<String, Object> result = compOffService.creditCompOff(
                    dto.getEmpId(), dto.getLeaveTypeName(), dto.getWorkedDate(),
                    dto.getReason(), dto.getCreditedBy());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam String empId) {
        try {
            CompOffBalanceDTO dto = compOffService.getAvailableBalance(empId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/expire-stale")
    public ResponseEntity<?> expireStale(@RequestParam(required = false) String leaveTypeName) {
        try {
            return ResponseEntity.ok(compOffService.expireStaleCompOff(leaveTypeName));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
