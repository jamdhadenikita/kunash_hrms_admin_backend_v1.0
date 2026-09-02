package com.hrms.repository;

import com.hrms.entity.OnboardingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingRepository extends JpaRepository<OnboardingEntity, Long> {

    Optional<OnboardingEntity> findByOnboardingId(String onboardingId);

    Optional<OnboardingEntity> findByEmployeePrimeId(Long employeePrimeId);

    Page<OnboardingEntity> findByStatus(String status, Pageable pageable);

    Page<OnboardingEntity> findByEmployeePrimeIdContaining(String search, Pageable pageable);

    // Pending onboardings (Document Pending / In Progress) — limit 5
    @Query("""
        SELECT o
        FROM OnboardingEntity o
        WHERE UPPER(o.status) NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY o.createdAt DESC
        """)
    List<OnboardingEntity> findTopPendingOnboardings(Pageable pageable);
    // Usage: findTopPendingOnboardings(PageRequest.of(0, 5))

    // Count pending onboardings for stats badge
    @Query("SELECT COUNT(o) FROM OnboardingEntity o WHERE UPPER(o.status) NOT IN ('COMPLETED', 'CANCELLED')")
    long countPendingOnboardings();

    @Query("SELECT AVG(DATEDIFF(o.onboardingCompletedDate, o.createdAt)) FROM OnboardingEntity o " +
            "WHERE o.onboardingCompletedDate IS NOT NULL")
    Double getAvgOnboardingCompletionDays();
}