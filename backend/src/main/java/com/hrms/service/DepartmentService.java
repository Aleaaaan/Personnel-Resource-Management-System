package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Department;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> list() {
        List<Department> departments = departmentRepository.findAll(Sort.by("id"));
        for (Department department : departments) {
            department.setEmployeeCount(employeeRepository.countByDepartmentId(department.getId()));
        }
        return departments;
    }

    @Transactional
    public Department create(Department department) {
        if (!StringUtils.hasText(department.getName())) {
            throw new BusinessException("部门名称不能为空");
        }
        if (departmentRepository.existsByName(department.getName())) {
            throw new BusinessException("部门名称已存在");
        }
        department.setId(null);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "部门不存在"));
        if (!StringUtils.hasText(department.getName())) {
            throw new BusinessException("部门名称不能为空");
        }
        if (departmentRepository.existsByNameAndIdNot(department.getName(), id)) {
            throw new BusinessException("部门名称已存在");
        }
        existing.setName(department.getName());
        existing.setCode(department.getCode());
        existing.setManagerName(department.getManagerName());
        existing.setDescription(department.getDescription());
        return departmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new BusinessException(404, "部门不存在");
        }
        if (employeeRepository.countByDepartmentId(id) > 0) {
            throw new BusinessException("该部门下仍有员工，无法删除");
        }
        departmentRepository.deleteById(id);
    }
}
