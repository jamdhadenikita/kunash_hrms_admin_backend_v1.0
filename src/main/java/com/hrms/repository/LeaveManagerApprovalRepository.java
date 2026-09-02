package com.hrms.repository;

import com.hrms.entity.LeaveManagerApprovalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveManagerApprovalRepository extends JpaRepository<LeaveManagerApprovalEntity, Long> {

    Optional<LeaveManagerApprovalEntity> findByLeaveRequestId(Long leaveRequestId);

    List<LeaveManagerApprovalEntity> findByEmpIdAndManagerApprovalStatus(String empId, String status);

    List<LeaveManagerApprovalEntity> findByManagerApprovalStatus(String status);
}