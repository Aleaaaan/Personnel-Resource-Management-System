package com.hrms.repository;

import com.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByEmpNo(String empNo);

    boolean existsByEmpNoAndIdNot(String empNo, Long id);

    long countByStatus(String status);

    long countByDepartmentId(Long departmentId);

    long countByPositionId(Long positionId);

    @Query(value = "select d.name, count(e.id) from department d left join employee e " +
            "on e.department_id = d.id and e.status = 'ACTIVE' group by d.id, d.name order by d.id", nativeQuery = true)
    List<Object[]> countByDepartment();

    @Query(value = "select e.education, count(e.id) from employee e where e.status = 'ACTIVE' " +
            "group by e.education", nativeQuery = true)
    List<Object[]> countByEducation();
}
