package com.hrms.repository;

import com.hrms.entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    Optional<EmployeeEntity> findByWorkEmail(String workEmail);

    Optional<EmployeeEntity> findByEmployeeId(String employeeId);

    Optional<EmployeeEntity> findByEmployeePrimeId(Long employeePrimeId);

    Optional<EmployeeEntity> findByPersonalEmail(String email);

    boolean existsByPersonalEmail(String email);

    boolean existsByEmployeePrimeId(String employeePrimeId);

    Page<EmployeeEntity> findByDepartment(String department, Pageable pageable);

    Page<EmployeeEntity> findByStatus(String status, Pageable pageable);

    // --- Bulk Upload: pre-fetch all emails in one query to avoid N+1 ---

    /**
     * Returns all existing personal emails (non-null) for duplicate detection.
     * Fetches only the email column — avoids loading full entities.
     */
    @Query("SELECT e.personalEmail FROM EmployeeEntity e WHERE e.personalEmail IS NOT NULL")
    List<String> findAllPersonalEmails();

    /**
     * Returns all existing work emails (non-null) for duplicate detection.
     */
    @Query("SELECT e.workEmail FROM EmployeeEntity e WHERE e.workEmail IS NOT NULL")
    List<String> findAllWorkEmails();

    /**
     * Returns all existing employeeIds for bulk-upload collision prevention.
     * Avoids duplicate EMP#### when there are gaps from deletions.
     */
    @Query("SELECT e.employeeId FROM EmployeeEntity e WHERE e.employeeId IS NOT NULL")
    List<String> findAllEmployeeIds();

    // --- Payroll queries ---

    @Query("""
        SELECT e FROM EmployeeEntity e
        WHERE UPPER(e.department) = UPPER(:department)
          AND (e.status IS NULL OR UPPER(e.status) = 'ACTIVE')
        ORDER BY e.firstName ASC
        """)
    List<EmployeeEntity> findActiveByDepartment(@Param("department") String department);

    @Query("""
        SELECT e FROM EmployeeEntity e
        WHERE (e.status IS NULL OR UPPER(e.status) = 'ACTIVE')
        ORDER BY e.firstName ASC
        """)
    List<EmployeeEntity> findAllActive();

    boolean existsByEmployeeId(String employeeId);



// Total active workforce count
long countByStatus(String status);          // countByStatus("ACTIVE")

// Department distribution — distinct departments + headcount, no duplicates
@Query("""
    SELECT e.department, COUNT(e)
    FROM EmployeeEntity e
    WHERE e.department IS NOT NULL
      AND (e.status IS NULL OR UPPER(e.status) = 'ACTIVE')
    GROUP BY e.department
    ORDER BY COUNT(e) DESC
    """)
List<Object[]> countActiveByDepartment();

// Recent new hires (last 7 days) for Recent Activity feed
@Query("""
    SELECT e
    FROM EmployeeEntity e
    WHERE e.createdAt >= :since
    ORDER BY e.createdAt DESC
    """)
List<EmployeeEntity> findRecentHires(@Param("since") LocalDate since);


//    @Query(value = "SELECT employee_id FROM employees WHERE employee_id REGEXP '^EMPSIEC[0-9]+$' ORDER BY CAST(SUBSTRING(employee_id, 8) AS UNSIGNED) DESC LIMIT 1", nativeQuery = true)
//    Optional<String> findTopEmployeeIdBySeries();

    @Query(value = "SELECT employee_id FROM employees WHERE employee_id REGEXP '^EMPKMS[0-9]+$' ORDER BY CAST(SUBSTRING(employee_id, 7) AS UNSIGNED) DESC LIMIT 1", nativeQuery = true)
    Optional<String> findTopEmployeeIdBySeries();

   /// ////////////////////////

    @Query("SELECT e FROM EmployeeEntity e WHERE e.dateOfBirth IS NOT NULL " +
            "AND MONTH(e.dateOfBirth) = MONTH(CURRENT_DATE) " +
            "AND DAY(e.dateOfBirth) = DAY(CURRENT_DATE) " +
            "AND e.status = 'ACTIVE'")
    List<EmployeeEntity> findTodaysBirthdays();

    @Query("SELECT e FROM EmployeeEntity e WHERE e.dateOfBirth IS NOT NULL AND e.status = 'ACTIVE'")
    List<EmployeeEntity> findAllActiveWithDateOfBirth();
}