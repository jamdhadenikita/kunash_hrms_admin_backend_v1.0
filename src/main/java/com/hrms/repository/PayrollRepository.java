package com.hrms.repository;

import com.hrms.entity.PayrollEntity;
import com.hrms.enum_status.PayrollMonth;
import com.hrms.enum_status.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Payroll repository.
 *
 * Production concerns addressed:
 * ─────────────────────────────────────────────────────────────────────────
 * 1. Duplicate check  → existsByEmployee_EmployeeIdAndPayrollMonthAndPayrollYear
 *    Prevents re-initiating pay for the same employee in the same month.
 *
 * 2. Bulk fetch with JOIN FETCH → avoids N+1 when loading payroll + employee.
 *
 * 3. Aggregation query for summary card → runs in DB, not in application layer.
 *
 * 4. Department filter is CASE-INSENSITIVE to tolerate data inconsistencies.
 *
 * 5. Status bulk-cancel for a month → supports month rollback scenarios.
 * ─────────────────────────────────────────────────────────────────────────
 */
@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, Long> {

    // ── Duplicate prevention ──────────────────────────────────────────────

    /**
     * Returns true if a payroll record already exists for this employee
     * in the given month/year, regardless of status.
     */
    boolean existsByEmployee_EmployeeIdAndPayrollMonthAndPayrollYear(
            String employeeId,
            PayrollMonth payrollMonth,
            Integer payrollYear
    );


    //get all payroll of all employee
    Page<PayrollEntity> findAll(Pageable pageable);

    // ── Single record lookup ──────────────────────────────────────────────

    /**
     * Fetch a specific payroll record for an employee in a given month.
     * JOIN FETCH avoids a lazy-load trip for the employee.
     */
    @Query("""
        SELECT p FROM PayrollEntity p
        JOIN FETCH p.employee e
        WHERE e.employeeId = :employeeId
          AND p.payrollMonth = :month
          AND p.payrollYear  = :year
        """)
    Optional<PayrollEntity> findByEmployeeIdAndMonthAndYear(
            @Param("employeeId") String employeeId,
            @Param("month")      PayrollMonth month,
            @Param("year")       Integer year
    );

    // ── List queries ──────────────────────────────────────────────────────

    /**
     * All payroll records for a given month/year (used for Salary List table).
     * JOIN FETCH prevents N+1 when iterating over the list to build response DTOs.
     */
    @Query("""
        SELECT p FROM PayrollEntity p
        JOIN FETCH p.employee e
        WHERE p.payrollMonth = :month
          AND p.payrollYear  = :year
        ORDER BY e.firstName ASC
        """)
    List<PayrollEntity> findAllByMonthAndYear(
            @Param("month") PayrollMonth month,
            @Param("year")  Integer year
    );

    /**
     * Filtered by department (case-insensitive).
     * Handles the "Select Department" dropdown on the frontend.
     */
    @Query("""
        SELECT p FROM PayrollEntity p
        JOIN FETCH p.employee e
        WHERE p.payrollMonth      = :month
          AND p.payrollYear       = :year
          AND UPPER(e.department) = UPPER(:department)
        ORDER BY e.firstName ASC
        """)
    List<PayrollEntity> findAllByMonthAndYearAndDepartment(
            @Param("month")       PayrollMonth month,
            @Param("year")        Integer year,
            @Param("department")  String department
    );

    /**
     * All payroll records for an employee across all months (payslip history).
     */
    @Query("""
        SELECT p FROM PayrollEntity p
        JOIN FETCH p.employee e
        WHERE e.employeeId = :employeeId
        ORDER BY p.payrollYear DESC, p.payrollMonth DESC
        """)
    List<PayrollEntity> findAllByEmployeeId(@Param("employeeId") String employeeId);

    // ── Summary aggregation ───────────────────────────────────────────────

    @Query("""
    SELECT
        COUNT(p),
        COALESCE(SUM(p.grossSalary),    0.0),
        COALESCE(SUM(p.totalDeductions), 0.0),
        COALESCE(SUM(p.netSalary),       0.0)
    FROM PayrollEntity p
    WHERE p.payrollMonth = :month
      AND p.payrollYear  = :year
      AND p.status NOT IN ('CANCELLED', 'FAILED')
    """)
    List<Object[]> getSummaryByMonthAndYear(
            @Param("month") PayrollMonth month,
            @Param("year")  Integer year
    );


    @Query("""
    SELECT
        COUNT(p),
        COALESCE(SUM(p.grossSalary),    0.0),
        COALESCE(SUM(p.totalDeductions), 0.0),
        COALESCE(SUM(p.netSalary),       0.0)
    FROM PayrollEntity p
    JOIN p.employee e
    WHERE p.payrollMonth      = :month
      AND p.payrollYear       = :year
      AND UPPER(e.department) = UPPER(:department)
      AND p.status NOT IN ('CANCELLED', 'FAILED')
    """)
    List<Object[]> getSummaryByMonthAndYearAndDepartment(
            @Param("month")      PayrollMonth month,
            @Param("year")       Integer year,
            @Param("department") String department
    );


    // ── Status management ─────────────────────────────────────────────────

    /**
     * Bulk status update for a month (e.g., mark all PROCESSED → PAID after bank transfer).
     * @Modifying + @Transactional (on service) to flush correctly.
     */
    @Modifying
    @Query("""
        UPDATE PayrollEntity p
        SET p.status = :newStatus, p.updatedAt = CURRENT_TIMESTAMP
        WHERE p.payrollMonth = :month
          AND p.payrollYear  = :year
          AND p.status = :currentStatus
        """)
    int bulkUpdateStatus(
            @Param("month")         PayrollMonth month,
            @Param("year")          Integer year,
            @Param("currentStatus") PayrollStatus currentStatus,
            @Param("newStatus") PayrollStatus newStatus
    );

    /**
     * Find all records by status for a given month (e.g., find FAILED records to retry).
     */
    List<PayrollEntity> findByPayrollMonthAndPayrollYearAndStatus(
            PayrollMonth month,
            Integer year,
            PayrollStatus status
    );

    @Query("""
    SELECT e.department, COALESCE(SUM(p.netSalary), 0.0), COALESCE(SUM(p.ctc), 0.0)
    FROM PayrollEntity p JOIN p.employee e
    WHERE p.payrollMonth = :month AND p.payrollYear = :year
      AND p.status NOT IN ('CANCELLED','FAILED')
    GROUP BY e.department
    """)
    List<Object[]> getDeptWiseCostAndCtc(@Param("month") PayrollMonth month, @Param("year") Integer year);

}
