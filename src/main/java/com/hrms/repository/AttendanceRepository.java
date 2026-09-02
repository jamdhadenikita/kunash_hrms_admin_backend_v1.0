package com.hrms.repository;

import com.hrms.entity.AttendanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

    // Find attendance by employee and specific date
    Optional<AttendanceEntity> findByEmployeeEmployeePrimeIdAndAttendanceDate(Long employeePrimeId, LocalDate date);

    // Find attendance by employee and date range
    List<AttendanceEntity> findByEmployeeEmployeePrimeIdAndAttendanceDateBetween(Long employeePrimeId, LocalDate startDate, LocalDate endDate);

    // Find all attendance for a specific date
    List<AttendanceEntity> findByAttendanceDate(LocalDate date);

    // Find attendance by date range
    List<AttendanceEntity> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

    // Find attendance by employee for a specific month
    @Query("SELECT a FROM AttendanceEntity a WHERE a.employee.employeePrimeId = :employeePrimeId AND YEAR(a.attendanceDate) = :year AND MONTH(a.attendanceDate) = :month")
    List<AttendanceEntity> findByEmployeeAndMonth(@Param("employeePrimeId") Long employeePrimeId, @Param("year") int year, @Param("month") int month);

    // Check if attendance already exists for employee on date
    boolean existsByEmployeeEmployeePrimeIdAndAttendanceDate(Long employeePrimeId, LocalDate date);

    // Delete all attendance records for a specific employee
    @Modifying
    @Query("DELETE FROM AttendanceEntity a WHERE a.employee.employeePrimeId = :employeePrimeId")
    void deleteByEmployeePrimeId(@Param("employeePrimeId") Long employeePrimeId);

    Page<AttendanceEntity> findByEmployeeEmployeePrimeId(Long employeePrimeId, Pageable pageable);


    @Query("SELECT a.status, COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate = :date GROUP BY a.status")
    List<Object[]> countByStatusForDate(@Param("date") LocalDate date);

    @Query("SELECT e.department, a.status, COUNT(a) FROM AttendanceEntity a JOIN a.employee e WHERE a.attendanceDate = :date GROUP BY e.department, a.status")
    List<Object[]> countByDeptAndStatusForDate(@Param("date") LocalDate date);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :from AND :to AND a.employee.department = :dept")
    List<AttendanceEntity> findByDateRangeAndDept(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("dept") String dept);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :from AND :to")
    List<AttendanceEntity> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);


    /**
     * Fetch a single day's record (used for today's stat card).
     * Alias of the existing method — already present, nothing to add.
     */
    // findByEmployeeEmployeePrimeIdAndAttendanceDate  ← already exists ✓

    /**
     * Sum of totalHours for a date range (used if you ever want a quick scalar).
     * Optional — service can sum in Java from the list query above.
     */
    @Query("""
        SELECT COALESCE(SUM(a.totalHours), 0.0)
        FROM AttendanceEntity a
        WHERE a.employee.employeePrimeId = :employeePrimeId
          AND a.attendanceDate BETWEEN :from AND :to
        """)
    Double sumHoursBetween(
            @Param("employeePrimeId") Long employeePrimeId,
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);


    // Present count for today  (status = 'Present')
    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate = :date AND UPPER(a.status) = 'PRESENT'")
    long countPresentOnDate(@Param("date") LocalDate date);

    // On-leave count today — employees with an APPROVED leave that covers today
    // Note: lives in LeaveRequestRepository (see below)

    // Attendance chart — per-day present & absent between two dates
    // Returns rows: [attendanceDate (LocalDate), status (String), count (Long)]
    @Query("""
        SELECT a.attendanceDate, a.status, COUNT(a)
        FROM AttendanceEntity a
        WHERE a.attendanceDate BETWEEN :from AND :to
        GROUP BY a.attendanceDate, a.status
        ORDER BY a.attendanceDate ASC """)
    List<Object[]> countByDateAndStatus(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByAttendanceDateAndStatusIgnoreCase(LocalDate attendanceDate, String status);

    List<AttendanceEntity> findByAttendanceDateAndCheckOutTimeIsNull(LocalDate attendanceDate);

    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :from AND :to " +
            "AND UPPER(a.status) = 'PRESENT'")
    long countPresentInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :from AND :to")
    long countTotalInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(a) FROM AttendanceEntity a WHERE a.attendanceDate BETWEEN :from AND :to " +
            "AND UPPER(a.status) = 'WFH'")
    long countWfhInRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}