package com.hrms.repository;

import com.hrms.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>,
        JpaSpecificationExecutor<LeaveRequest> {

    long countByStatus(String status);

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
}
