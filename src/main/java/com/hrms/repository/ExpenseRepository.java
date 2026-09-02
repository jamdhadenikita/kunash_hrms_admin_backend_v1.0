package com.hrms.repository;

import com.hrms.entity.ExpenseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByEmployeeEmployeePrimeId(Long employeePrimeId);

    List<ExpenseEntity> findByStatus(String status);

    List<ExpenseEntity> findByExpenseType(String expenseType);

    List<ExpenseEntity> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM ExpenseEntity e WHERE e.status = :status ORDER BY e.submittedDate DESC")
    List<ExpenseEntity> findPendingApprovals(@Param("status") String status);

    @Query("SELECT SUM(e.amount) FROM ExpenseEntity e WHERE e.status = 'Approved' AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    Double getTotalApprovedExpensesByMonth(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT e.expenseType, SUM(e.amount) FROM ExpenseEntity e WHERE e.status = 'Approved' GROUP BY e.expenseType")
    List<Object[]> getExpenseSummaryByType();


    /**
     * Sum of approved expenses for an employee within a date range (pay month).
     * Used exclusively by PayrollComputationService.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
            "WHERE e.employee.employeePrimeId = :employeePrimeId " +
            "AND e.status = 'Approved' " +
            "AND e.expenseDate BETWEEN :startDate AND :endDate")
    Double getTotalApprovedExpensesByEmployeeAndMonth(
            @Param("employeePrimeId") Long employeePrimeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    // Pending expense approvals (limit 5, newest first) — for Pending Approvals widget
    @Query("""
            SELECT e
            FROM ExpenseEntity e
            WHERE UPPER(e.status) = 'PENDING'
            ORDER BY e.submittedDate DESC
            """)
    List<ExpenseEntity> findTopPendingExpenses(Pageable pageable);
    // Usage: findTopPendingExpenses(PageRequest.of(0, 5))

    // Count all pending expenses
    long countByStatus(String status);


    @Query(value = "SELECT MAX(CAST(SUBSTRING(claim_id, 5) AS UNSIGNED)) FROM expense_claims WHERE claim_id REGEXP '^EXP-[0-9]+$'", nativeQuery = true)
    Long findMaxClaimSuffix();




    /**
     * Find approved expenses by employee with submitted date <= 26th of the month
     */
    @Query("""
    SELECT e FROM ExpenseEntity e 
    WHERE e.employee.employeePrimeId = :employeePrimeId 
    AND e.status = 'Approved'
    AND DAY(e.submittedDate) <= 26
    AND MONTH(e.submittedDate) = :month
    AND YEAR(e.submittedDate) = :year
    ORDER BY e.submittedDate DESC
    """)
    List<ExpenseEntity> findApprovedExpensesByEmployeeAndMonth(
            @Param("employeePrimeId") Long employeePrimeId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    /**
     * Get total sum of approved expenses for an employee with submitted date <= 26th of the month
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
            "WHERE e.employee.employeePrimeId = :employeePrimeId " +
            "AND e.status = 'Approved' " +
            "AND DAY(e.submittedDate) <= 26 " +
            "AND MONTH(e.submittedDate) = :month " +
            "AND YEAR(e.submittedDate) = :year")
    Double getTotalApprovedAmountByEmployeeAndMonth(
            @Param("employeePrimeId") Long employeePrimeId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("SELECT e.status, COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e " +
            "WHERE e.expenseDate BETWEEN :from AND :to GROUP BY e.status")
    List<Object[]> sumAmountByStatusInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
