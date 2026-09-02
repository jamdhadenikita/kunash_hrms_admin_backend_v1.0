package com.hrms.repository;

import com.hrms.entity.CompOffEarnedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompOffEarnedRepository extends JpaRepository<CompOffEarnedEntity, Long> {

    List<CompOffEarnedEntity> findByEmpId(String empId);

    List<CompOffEarnedEntity> findByEmpIdAndStatus(String empId, String status);

    Optional<CompOffEarnedEntity> findByEmpIdAndWorkedDate(String empId, LocalDate workedDate);

    List<CompOffEarnedEntity> findByStatusAndExpiryDateBefore(String status, LocalDate date);
}