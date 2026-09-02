package com.hrms.controller;

import com.hrms.dto.response.CarryForwardBalanceDTO;
import com.hrms.service.LeaveCarryForwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────
 *  CASUAL-LEAVE-STYLE CARRY-FORWARD API  (new, additive)
 * ─────────────────────────────────────────────────────────────
 *  POST /api/leave/carry-forward/process?fromYear=2025   → HR runs year-end rollover
 *  POST /api/leave/carry-forward/lapse-expired            → HR runs the 6-month-lapse sweep
 *  GET  /api/leave/carry-forward/balance?empId=&leaveType=&year=
 *  GET  /api/leave/carry-forward/balance/all?empId=&year=
 * ─────────────────────────────────────────────────────────────
 *  "process" and "lapse-expired" can be called manually by HR from a
 *  button in the UI, or wired to a @Scheduled job later — no change to
 *  any of your existing files is required either way.
 */
@RestController
@RequestMapping("/api/leave/carry-forward")
public class LeaveCarryForwardController {

    @Autowired
    private LeaveCarryForwardService leaveCarryForwardService;

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestParam(required = false) Integer fromYear) {
        try {
            int year = (fromYear != null) ? fromYear : LocalDate.now().getYear() - 1;
            return ResponseEntity.ok(leaveCarryForwardService.processYearEndCarryForward(year));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/lapse-expired")
    public ResponseEntity<?> lapseExpired() {
        try {
            return ResponseEntity.ok(leaveCarryForwardService.lapseExpiredCarryForwards());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(
            @RequestParam String empId,
            @RequestParam String leaveType,
            @RequestParam(defaultValue = "0") int year) {
        try {
            int y = year > 0 ? year : LocalDate.now().getYear();
            CarryForwardBalanceDTO dto = leaveCarryForwardService.getCarryForwardBalance(empId, leaveType, y);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/balance/all")
    public ResponseEntity<List<CarryForwardBalanceDTO>> getAllBalances(
            @RequestParam String empId,
            @RequestParam(defaultValue = "0") int year) {
        int y = year > 0 ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(leaveCarryForwardService.getAllCarryForwardBalances(empId, y));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/leave/carry-forward/manual-entry
    //
    // ONE-TIME MIGRATION endpoint — for entering each employee's actual
    // carried-forward balance (from pre-system records) directly, since
    // this system has no historical year to auto-calculate from yet.
    // Every employee's value is different — call this once per employee
    // with whatever number HR has on file.
    //
    // Request Body:
    // {
    //   "empId": "EMPSIEC01",
    //   "leaveType": "Casual Leave",
    //   "toYear": 2026,
    //   "carriedDays": 6,
    //   "expiryDate": "2026-07-01"   // optional — computed from policy if omitted
    // }
    // ─────────────────────────────────────────────────────────
    @PostMapping("/manual-entry")
    public ResponseEntity<?> manualEntry(@RequestBody com.hrms.dto.request.ManualCarryForwardEntryDTO dto) {
        try {
            Map<String, Object> result = leaveCarryForwardService.manualCarryForwardEntry(
                    dto.getEmpId(), dto.getLeaveType(), dto.getToYear(),
                    dto.getCarriedDays(), dto.getExpiryDate());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

