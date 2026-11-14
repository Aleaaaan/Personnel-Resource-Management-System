package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.Salary;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.SalaryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public SalaryService(SalaryRepository salaryRepository,
                         EmployeeRepository employeeRepository,
                         DepartmentRepository departmentRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Page<Salary> page(Long employeeId, String month, String status, int page, int size) {
        Specification<Salary> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (StringUtils.hasText(month)) {
                predicates.add(cb.equal(root.get("salaryMonth"), month));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Salary> result = salaryRepository.findAll(specification,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "salaryMonth", "id")));
        fillNames(result.getContent());
        return result;
    }

    /**
     * 按月份为所有在职员工批量生成工资单，已存在的员工跳过
     */
    @Transactional
    public Map<String, Object> generate(String month) {
        try {
            YearMonth.parse(month);
        } catch (Exception e) {
            throw new BusinessException("薪资月份格式不正确，应为 yyyy-MM");
        }
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .collect(Collectors.toList());
        int createdCount = 0;
        for (Employee employee : employees) {
            if (salaryRepository.existsByEmployeeIdAndSalaryMonth(employee.getId(), month)) {
                continue;
            }
            Salary salary = new Salary();
            salary.setEmployeeId(employee.getId());
            salary.setSalaryMonth(month);
            BigDecimal base = employee.getBaseSalary() == null ? BigDecimal.ZERO : employee.getBaseSalary();
            salary.setBaseSalary(base);
            salary.setBonus(BigDecimal.ZERO);
            salary.setAllowance(new BigDecimal("800"));
            salary.setDeduction(BigDecimal.ZERO);
            BigDecimal social = base.multiply(new BigDecimal("0.105")).setScale(2, RoundingMode.HALF_UP);
            salary.setSocialSecurity(social);
            BigDecimal taxable = base.subtract(social).subtract(new BigDecimal("5000"));
            BigDecimal tax = taxable.compareTo(BigDecimal.ZERO) > 0
                    ? taxable.multiply(new BigDecimal("0.03")).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            salary.setTax(tax);
            salary.setNetSalary(calculateNet(base, salary.getBonus(), salary.getAllowance(),
                    salary.getDeduction(), social, tax));
            salary.setStatus("DRAFT");
            salary.setCreatedAt(LocalDateTime.now());
            salaryRepository.save(salary);
            createdCount++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("created", createdCount);
        result.put("skipped", employees.size() - createdCount);
        return result;
    }

    @Transactional
    public Salary update(Long id, Salary request) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工资单不存在"));
        if ("PAID".equals(salary.getStatus())) {
            throw new BusinessException("已发放的工资单不能修改");
        }
        if (request.getBonus() != null) {
            salary.setBonus(request.getBonus());
        }
        if (request.getAllowance() != null) {
            salary.setAllowance(request.getAllowance());
        }
        if (request.getDeduction() != null) {
            salary.setDeduction(request.getDeduction());
        }
        salary.setNetSalary(calculateNet(salary.getBaseSalary(), salary.getBonus(), salary.getAllowance(),
                salary.getDeduction(), salary.getSocialSecurity(), salary.getTax()));
        Salary saved = salaryRepository.save(salary);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public Salary pay(Long id) {
        Salary salary = salaryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "工资单不存在"));
        if ("PAID".equals(salary.getStatus())) {
            throw new BusinessException("该工资单已发放");
        }
        salary.setStatus("PAID");
        salary.setPayDate(LocalDate.now());
        Salary saved = salaryRepository.save(salary);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public Map<String, Object> payBatch(String month) {
        List<Salary> salaries = salaryRepository.findBySalaryMonth(month);
        int paidCount = 0;
        for (Salary salary : salaries) {
            if (!"PAID".equals(salary.getStatus())) {
                salary.setStatus("PAID");
                salary.setPayDate(LocalDate.now());
                salaryRepository.save(salary);
                paidCount++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("paid", paidCount);
        return result;
    }

    private BigDecimal calculateNet(BigDecimal base, BigDecimal bonus, BigDecimal allowance,
                                    BigDecimal deduction, BigDecimal socialSecurity, BigDecimal tax) {
        return nullToZero(base)
                .add(nullToZero(bonus))
                .add(nullToZero(allowance))
                .subtract(nullToZero(deduction))
                .subtract(nullToZero(socialSecurity))
                .subtract(nullToZero(tax))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void fillNames(Collection<Salary> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> employeeIds = records.stream().map(Salary::getEmployeeId).distinct()
                .collect(Collectors.toList());
        Map<Long, Employee> employeeMap = new HashMap<>();
        employeeRepository.findAllById(employeeIds).forEach(e -> employeeMap.put(e.getId(), e));
        Map<Long, String> departmentMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        for (Salary record : records) {
            Employee employee = employeeMap.get(record.getEmployeeId());
            if (employee != null) {
                record.setEmployeeName(employee.getName());
                record.setEmpNo(employee.getEmpNo());
                record.setDepartmentName(departmentMap.get(employee.getDepartmentId()));
            }
        }
    }
}
