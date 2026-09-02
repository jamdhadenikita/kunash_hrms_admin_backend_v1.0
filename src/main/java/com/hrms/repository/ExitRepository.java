package com.hrms.repository;

import com.hrms.entity.EmployeeEntity;
import com.hrms.entity.ExitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExitRepository extends JpaRepository<ExitEntity, Long> {

    Optional<ExitEntity> findByExitId(String exitId);

    List<ExitEntity> findByEmployeeEmployeePrimeId(Long employeeId);

    Page<ExitEntity> findByStatus(String status, Pageable pageable);

    // ✅ FIXED: Use e.employee.xxx instead of direct fields
    @Query("SELECT e FROM ExitEntity e WHERE " +
            "LOWER(e.employee.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.employee.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.employee.department) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "e.exitId LIKE CONCAT('%', :search, '%')")
    Page<ExitEntity> searchExits(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(e) FROM ExitEntity e WHERE e.status = 'PENDING'")
    long countPendingResignations();

    @Query("SELECT COUNT(e) FROM ExitEntity e WHERE e.status = 'APPROVED'")
    long countApprovedResignations();

    @Query("SELECT COUNT(e) FROM ExitEntity e WHERE e.status = 'COMPLETED' AND YEAR(e.createdAt) = YEAR(CURRENT_DATE)")
    long countCompletedExitsThisYear();

    @Query("SELECT COUNT(e) FROM ExitEntity e WHERE e.status IN ('APPROVED', 'NOTICE_PERIOD')")
    long countActiveResignations();

    @Query("SELECT MAX(e.exitId) FROM ExitEntity e WHERE e.exitId LIKE 'EXT%'")
    String findMaxExitId();

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.itClearance = :cleared WHERE e.id = :id")
    void updateITClearance(@Param("id") Long id, @Param("cleared") Boolean cleared);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.hrClearance = :cleared WHERE e.id = :id")
    void updateHRClearance(@Param("id") Long id, @Param("cleared") Boolean cleared);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.financeClearance = :cleared WHERE e.id = :id")
    void updateFinanceClearance(@Param("id") Long id, @Param("cleared") Boolean cleared);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.adminClearance = :cleared WHERE e.id = :id")
    void updateAdminClearance(@Param("id") Long id, @Param("cleared") Boolean cleared);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.basicSalary = :basicSalary, e.leaveEncashment = :leaveEncashment, " +
            "e.bonus = :bonus, e.allowances = :allowances, e.totalEarnings = :totalEarnings, " +
            "e.salaryAdvance = :salaryAdvance, e.loanRecovery = :loanRecovery, e.assetDamage = :assetDamage, " +
            "e.otherDeductions = :otherDeductions, e.totalDeductions = :totalDeductions, " +
            "e.netPayable = :netPayable, e.amountInWords = :amountInWords WHERE e.id = :id")
    void updateSettlement(@Param("id") Long id,
                          @Param("basicSalary") Double basicSalary,
                          @Param("leaveEncashment") Double leaveEncashment,
                          @Param("bonus") Double bonus,
                          @Param("allowances") Double allowances,
                          @Param("totalEarnings") Double totalEarnings,
                          @Param("salaryAdvance") Double salaryAdvance,
                          @Param("loanRecovery") Double loanRecovery,
                          @Param("assetDamage") Double assetDamage,
                          @Param("otherDeductions") Double otherDeductions,
                          @Param("totalDeductions") Double totalDeductions,
                          @Param("netPayable") Double netPayable,
                          @Param("amountInWords") String amountInWords);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.settlementDate = CURRENT_DATE, e.settlementProcessedBy = :processedBy, " +
            "e.status = 'COMPLETED' WHERE e.id = :id")
    void processSettlement(@Param("id") Long id, @Param("processedBy") String processedBy);

    @Modifying
    @Transactional
    @Query("UPDATE ExitEntity e SET e.interviewFeedback = :feedback, e.rating = :rating, " +
            "e.recommendToOthers = :recommend, e.conductedBy = :conductedBy, e.interviewDate = CURRENT_TIMESTAMP " +
            "WHERE e.id = :id")
    void updateExitInterview(@Param("id") Long id,
                             @Param("feedback") String feedback,
                             @Param("rating") Integer rating,
                             @Param("recommend") String recommend,
                             @Param("conductedBy") String conductedBy);

    @Query("SELECT e.employee.department, COUNT(e) FROM ExitEntity e GROUP BY e.employee.department")
    List<Object[]> getDepartmentWiseExitCount();

    Optional<ExitEntity> findByEmployeeAndStatus(EmployeeEntity employee, String status);


    // Add this method in ExitRepository interface
    Optional<ExitEntity> findByEmployeeAndStatusIn(EmployeeEntity employee, List<String> statuses);

    @Query("SELECT MONTH(e.createdAt), COUNT(e) FROM ExitEntity e WHERE YEAR(e.createdAt) = YEAR(CURRENT_DATE) GROUP BY MONTH(e.createdAt)")
    List<Object[]> getMonthlyExitCount();

    @Query("""
    SELECT AVG(DATEDIFF(e.createdAt, emp.joiningDate))
    FROM ExitEntity e JOIN e.employee emp
    WHERE e.status = 'COMPLETED' AND YEAR(e.createdAt) = :year
    """)
    Double getAvgTenureAtExitDays(@Param("year") int year);


    @Query("SELECT e.reasonForLeaving, COUNT(e) FROM ExitEntity e " +
            "WHERE e.reasonForLeaving IS NOT NULL AND YEAR(e.resignationDate) = :year " +
            "GROUP BY e.reasonForLeaving")
    List<Object[]> getReasonWiseExitCount(@Param("year") int year);

}