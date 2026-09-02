package com.hrms.repository;

import com.hrms.entity.AssetEntity;
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
public interface AssetRepository extends JpaRepository<AssetEntity, Long> {

    Optional<AssetEntity> findByAssetId(String assetId);

    Optional<AssetEntity> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByAssetId(String assetId);

    Page<AssetEntity> findByStatus(String status, Pageable pageable);

    Page<AssetEntity> findByCategory(String category, Pageable pageable);

    // ✅ FIXED: Use assignedToEmployee IS NULL instead of assignedToEmployeeId
    List<AssetEntity> findByStatusAndAssignedToEmployeeIsNull(String status);

    // ❌ DELETE THIS LINE:
    // Page<AssetEntity> findByAssignedToEmployeeId(String employeeId, Pageable pageable);

    // ✅ REPLACE WITH:
    Page<AssetEntity> findByAssignedToEmployee_EmployeePrimeId(Long employeePrimeId, Pageable pageable);

    // ✅ OR for String employeeId (EMP001):
    Page<AssetEntity> findByAssignedToEmployee_EmployeeId(String employeeId, Pageable pageable);

    Page<AssetEntity> findByRequestStatus(String requestStatus, Pageable pageable);

    Page<AssetEntity> findByMaintenanceStatus(String maintenanceStatus, Pageable pageable);

    @Query("SELECT a FROM AssetEntity a WHERE " +
            "LOWER(a.assetName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.assetId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AssetEntity> searchAssets(@Param("search") String search, Pageable pageable);

    @Query("SELECT a.status, COUNT(a) FROM AssetEntity a GROUP BY a.status")
    List<Object[]> getAssetStatusCount();

    @Query("SELECT a.category, COUNT(a) FROM AssetEntity a GROUP BY a.category")
    List<Object[]> getAssetCategoryCount();

    // ========== NEW METHODS FOR ID GENERATION ==========

    @Query("SELECT MAX(a.assetId) FROM AssetEntity a WHERE a.assetId LIKE 'AST%'")
    String findMaxAssetId();

    @Query("SELECT MAX(a.requestId) FROM AssetEntity a WHERE a.requestId LIKE 'REQ%'")
    String findMaxRequestId();

    @Query("SELECT MAX(a.maintenanceId) FROM AssetEntity a WHERE a.maintenanceId LIKE 'MNT%'")
    String findMaxMaintenanceId();


    // Recent maintenance records — for Recent Activity widget
    @Query("""
    SELECT a
    FROM AssetEntity a
    WHERE a.maintenanceDate >= :since
      AND a.maintenanceStatus IS NOT NULL
    ORDER BY a.maintenanceDate DESC
    """)
    List<AssetEntity> findRecentMaintenanceRecords(@Param("since") LocalDate since);

    @Query("SELECT COUNT(a) FROM AssetEntity a WHERE a.status = 'ASSIGNED' " +
            "AND a.expectedReturnDate < :today AND a.actualReturnDate IS NULL")
    long countOverdueReturns(@Param("today") LocalDate today);


}


