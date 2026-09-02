package com.hrms.repository;

import com.hrms.entity.LeaveRequestEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequestEntity, Long> {

    // ── Pending tab ──────────────────────────────────────────────────
    List<LeaveRequestEntity> findByStatus(String status);

    List<LeaveRequestEntity> findByStatusAndDepartment(String status, String department);

    // ── History tab ──────────────────────────────────────────────────
    List<LeaveRequestEntity> findByAddedOnBetween(LocalDate from, LocalDate to);

    List<LeaveRequestEntity> findByStatusAndAddedOnBetween(String status, LocalDate from, LocalDate to);

    // ── Employee dashboard ───────────────────────────────────────────
    List<LeaveRequestEntity> findByEmpId(String empId);

    // Used for overlap check: fetch all active (pending+approved) requests of employee
    List<LeaveRequestEntity> findByEmpIdAndStatusIn(String empId, List<String> statuses);

    // ── Balance calculation ──────────────────────────────────────────
    // Sum of approved days for a given employee + leaveType + year
    @Query("SELECT COALESCE(SUM(r.numberOfDays), 0) FROM LeaveRequestEntity r " +
            "WHERE r.empId = :empId " +
            "AND r.leaveType = :leaveType " +
            "AND r.status = 'approved' " +
            "AND YEAR(r.fromDate) = :year")
    int sumUsedDaysByEmpIdAndLeaveTypeAndYear(
            @Param("empId") String empId,
            @Param("leaveType") String leaveType,
            @Param("year") int year);

    // ── Pie chart ────────────────────────────────────────────────────
    @Query("SELECT r.department, SUM(r.numberOfDays) FROM LeaveRequestEntity r " +
            "WHERE r.status = 'approved' GROUP BY r.department")
    List<Object[]> sumApprovedDaysByDepartment();

    // ── Dashboard stat cards ─────────────────────────────────────────
    @Query("SELECT COUNT(r) FROM LeaveRequestEntity r " +
            "WHERE r.status = :status " +
            "AND r.actionDate BETWEEN :from AND :to")
    long countByStatusAndActionDateBetween(
            @Param("status") String status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(r.numberOfDays), 0) FROM LeaveRequestEntity r " +
            "WHERE r.status = 'approved' " +
            "AND r.actionDate BETWEEN :from AND :to")
    int sumApprovedDaysInRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);


    // Employees on approved leave TODAY
    @Query("""
    SELECT COUNT(DISTINCT lr.empId)
    FROM LeaveRequestEntity lr
    WHERE UPPER(lr.status) = 'APPROVED'
      AND :today BETWEEN lr.fromDate AND lr.toDate
    """)
    long countEmployeesOnLeaveToday(@Param("today") LocalDate today);

    // Pending leave requests (limit 5, newest first) — for Pending Approvals widget
    @Query("""
        SELECT lr
        FROM LeaveRequestEntity lr
        WHERE UPPER(lr.status) = 'PENDING'
        ORDER BY lr.addedOn DESC
        """)
    List<LeaveRequestEntity> findTopPendingLeaves(Pageable pageable);
    // Usage: findTopPendingLeaves(PageRequest.of(0, 5))

    // Recent approved leaves (last 7 days) — for Recent Activity widget
    @Query("""
    SELECT lr
    FROM LeaveRequestEntity lr
    WHERE UPPER(lr.status) = 'APPROVED'
      AND lr.actionDate >= :since
    ORDER BY lr.actionDate DESC
    """)
    List<LeaveRequestEntity> findRecentApprovedLeaves(@Param("since") LocalDate since);

    // Count all pending leaves (for stats badge)
    long countByStatus(String status);


    @Query("SELECT r.leaveType, COUNT(r) FROM LeaveRequestEntity r " +
            "WHERE r.status = 'approved' AND r.actionDate BETWEEN :from AND :to " +
            "GROUP BY r.leaveType")
    List<Object[]> countApprovedByLeaveTypeInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}