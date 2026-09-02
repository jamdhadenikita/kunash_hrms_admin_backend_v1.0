package com.hrms.scheduler.leave;

import com.hrms.service.LeaveCarryForwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────────
 *  AUTOMATIC YEAR-END CARRY-FORWARD  (the only scheduled job)
 * ─────────────────────────────────────────────────────────────
 * This runs the year-end carry-forward calculation automatically once
 * a year, so HR doesn't have to remember to trigger it manually. HR can
 * still trigger it manually as a backup via:
 *
 *   POST /api/leave/carry-forward/process?fromYear=YYYY
 *
 * (idempotent — running it again for the same year just updates the
 * existing record instead of duplicating it).
 *
 * Lapsing expired carry-forward and expiring stale Comp Off are NOT
 * scheduled jobs — they're handled lazily instead:
 *   - LeaveCarryForwardServiceImpl.getCarryForwardBalance() marks a
 *     bucket LAPSED the moment anyone reads it past its expiry date.
 *   - LeaveCarryForwardServiceImpl.deductFromCarryForwardFirst() already
 *     ignores expired buckets when deducting, regardless of whether
 *     they've been marked LAPSED yet — so expiry is enforced correctly
 *     even if nobody has "read" that balance recently.
 *   - CompOffServiceImpl.getAvailableBalance() similarly expires stale
 *     Comp Off entries (and reverses the linked leave balance) the
 *     moment anyone checks that employee's balance.
 * This means correctness never depends on a scheduler running — no
 * separate daily/monthly sweep job is needed for either of those.
 *
 * Nothing here is hardcoded except the cron TIME, which is standard
 * practice for schedulers — the whole job can be switched off via
 * application.properties without touching code:
 *
 *   leave.scheduler.enabled=true
 *
 * Requires @EnableScheduling on your main Spring Boot application class
 * (one annotation, add if not already present) — see README for the
 * exact line.
 */
@Component
public class LeavePolicyScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeavePolicyScheduler.class);

    @Autowired private LeaveCarryForwardService leaveCarryForwardService;

    @Value("${leave.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    // ─────────────────────────────────────────────────────────
    // Runs once a year, 1 January at 01:00 AM.
    // Processes carry-forward for the year that just ended
    // (i.e. on 1-Jan-2027 it processes fromYear=2026).
    // Cron: sec min hour day month day-of-week
    // ─────────────────────────────────────────────────────────
    @Scheduled(cron = "${leave.scheduler.carry-forward-cron:0 0 1 1 1 *}")
    public void runYearEndCarryForward() {
        if (!schedulerEnabled) {
            log.info("[LeavePolicyScheduler] Skipped — leave.scheduler.enabled=false");
            return;
        }
        int previousYear = LocalDate.now().getYear() - 1;
        try {
            Map<String, Object> result = leaveCarryForwardService.processYearEndCarryForward(previousYear);
            log.info("[LeavePolicyScheduler] Year-end carry-forward completed for {} -> {}",
                    previousYear, result);
        } catch (Exception e) {
            // Never let a scheduler failure crash the app — log and move on.
            // HR still has the manual endpoint as a fallback if this fails.
            log.error("[LeavePolicyScheduler] Year-end carry-forward FAILED for fromYear={}. " +
                            "HR should run POST /api/leave/carry-forward/process?fromYear={} manually.",
                    previousYear, previousYear, e);
        }
    }
}