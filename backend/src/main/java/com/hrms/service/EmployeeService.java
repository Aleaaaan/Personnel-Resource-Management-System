package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.Position;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    public Page<Employee> page(String keyword, Long departmentId, String status, int page, int size) {
        Specification<Employee> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("empNo"), like),
                        cb.like(root.get("phone"), like)));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Employee> result = employeeRepository.findAll(specification,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        fillNames(result.getContent());
        return result;
    }

    public Employee get(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "员工不存在"));
        fillNames(java.util.Collections.singletonList(employee));
        return employee;
    }

    @Transactional
    public Employee create(Employee employee) {
        validate(employee, null);
        employee.setId(null);
        if (!StringUtils.hasText(employee.getStatus())) {
            employee.setStatus("ACTIVE");
        }
        Employee saved = employeeRepository.save(employee);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public Employee update(Long id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "员工不存在"));
        validate(employee, id);
        existing.setEmpNo(employee.getEmpNo());
        existing.setName(employee.getName());
        existing.setGender(employee.getGender());
        existing.setBirthday(employee.getBirthday());
        existing.setPhone(employee.getPhone());
        existing.setEmail(employee.getEmail());
        existing.setIdCard(employee.getIdCard());
        existing.setDepartmentId(employee.getDepartmentId());
        existing.setPositionId(employee.getPositionId());
        existing.setHireDate(employee.getHireDate());
        existing.setStatus(employee.getStatus());
        existing.setBaseSalary(employee.getBaseSalary());
        existing.setEducation(employee.getEducation());
        existing.setAddress(employee.getAddress());
        Employee saved = employeeRepository.save(existing);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new BusinessException(404, "员工不存在");
        }
        employeeRepository.deleteById(id);
    }

    public List<Employee> allActive() {
        return employeeRepository.findAll(Sort.by("id")).stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .collect(Collectors.toList());
    }

    private void validate(Employee employee, Long id) {
        if (!StringUtils.hasText(employee.getEmpNo())) {
            throw new BusinessException("工号不能为空");
        }
        if (!StringUtils.hasText(employee.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        boolean duplicated = id == null
                ? employeeRepository.existsByEmpNo(employee.getEmpNo())
                : employeeRepository.existsByEmpNoAndIdNot(employee.getEmpNo(), id);
        if (duplicated) {
            throw new BusinessException("工号已存在：" + employee.getEmpNo());
        }
    }

    private void fillNames(Collection<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return;
        }
        Map<Long, String> departmentMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        Map<Long, String> positionMap = positionRepository.findAll().stream()
                .collect(Collectors.toMap(Position::getId, Position::getName, (a, b) -> a));
        for (Employee employee : employees) {
            employee.setDepartmentName(departmentMap.get(employee.getDepartmentId()));
            employee.setPositionName(positionMap.get(employee.getPositionId()));
        }
    }

    public Map<Long, Employee> mapByIds(Collection<Long> ids) {
        Map<Long, Employee> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        employeeRepository.findAllById(ids).forEach(e -> map.put(e.getId(), e));
        return map;
    }
}
