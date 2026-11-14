package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.dto.LeaveApprovalRequest;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.SysUser;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public LeaveService(LeaveRequestRepository leaveRepository,
                        EmployeeRepository employeeRepository,
                        DepartmentRepository departmentRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Page<LeaveRequest> page(Long employeeId, String status, int page, int size) {
        Specification<LeaveRequest> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<LeaveRequest> result = leaveRepository.findAll(specification,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        fillNames(result.getContent());
        return result;
    }

    @Transactional
    public LeaveRequest create(SysUser user, LeaveRequest request) {
        if (user.getEmployeeId() == null) {
            throw new BusinessException("当前账号未关联员工档案，无法提交申请");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("请选择请假起止日期");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("请选择请假类型");
        }
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployeeId(user.getEmployeeId());
        leave.setType(request.getType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setDays(BigDecimal.valueOf(days));
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        LeaveRequest saved = leaveRepository.save(leave);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public LeaveRequest approve(Long id, LeaveApprovalRequest request, SysUser approver) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "请假申请不存在"));
        if (!"PENDING".equals(leave.getStatus())) {
            throw new BusinessException("该申请已审批，请勿重复操作");
        }
        if (!"APPROVED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new BusinessException("审批结果不合法");
        }
        leave.setStatus(request.getStatus());
        leave.setApproverName(approver.getRealName());
        leave.setApproveTime(LocalDateTime.now());
        leave.setComment(request.getComment());
        LeaveRequest saved = leaveRepository.save(leave);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public void delete(Long id, SysUser user) {
        LeaveRequest leave = leaveRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "请假申请不存在"));
        if (!"PENDING".equals(leave.getStatus())) {
            throw new BusinessException("已审批的申请不能撤回");
        }
        if (!isHr(user) && !leave.getEmployeeId().equals(user.getEmployeeId())) {
            throw new BusinessException(403, "无权撤回他人的申请");
        }
        leaveRepository.delete(leave);
    }

    private boolean isHr(SysUser user) {
        return "ADMIN".equals(user.getRole()) || "HR".equals(user.getRole());
    }

    private void fillNames(Collection<LeaveRequest> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> employeeIds = records.stream().map(LeaveRequest::getEmployeeId).distinct()
                .collect(Collectors.toList());
        Map<Long, Employee> employeeMap = new HashMap<>();
        employeeRepository.findAllById(employeeIds).forEach(e -> employeeMap.put(e.getId(), e));
        Map<Long, String> departmentMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        for (LeaveRequest record : records) {
            Employee employee = employeeMap.get(record.getEmployeeId());
            if (employee != null) {
                record.setEmployeeName(employee.getName());
                record.setEmpNo(employee.getEmpNo());
                record.setDepartmentName(departmentMap.get(employee.getDepartmentId()));
            }
        }
    }
}
