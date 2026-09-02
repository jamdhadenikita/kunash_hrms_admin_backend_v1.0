package com.hrms.repository;

import com.hrms.entity.LeavePolicyConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeavePolicyConfigRepository extends JpaRepository<LeavePolicyConfigEntity, Long> {

    Optional<LeavePolicyConfigEntity> findByLeaveTypeNameAndEmployeeTypeAndDepartment(
            String leaveTypeName, String employeeType, String department);

    List<LeavePolicyConfigEntity> findByLeaveTypeName(String leaveTypeName);

    List<LeavePolicyConfigEntity> findByEmployeeTypeAndDepartment(String employeeType, String department);
}
