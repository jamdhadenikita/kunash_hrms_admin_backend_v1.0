package com.hrms.repository;

import com.hrms.entity.EmployeeLeaveCarryForwardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeLeaveCarryForwardRepository extends JpaRepository<EmployeeLeaveCarryForwardEntity, Long> {

    Optional<EmployeeLeaveCarryForwardEntity> findByEmpIdAndLeaveTypeAndFromYear(
            String empId, String leaveType, int fromYear);

    List<EmployeeLeaveCarryForwardEntity> findByEmpIdAndLeaveTypeAndToYear(
            String empId, String leaveType, int toYear);

    List<EmployeeLeaveCarryForwardEntity> findByStatusAndExpiryDateBefore(String status, java.time.LocalDate date);

    List<EmployeeLeaveCarryForwardEntity> findByFromYear(int fromYear);
}
