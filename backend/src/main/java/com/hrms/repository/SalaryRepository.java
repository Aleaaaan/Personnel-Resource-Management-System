package com.hrms.repository;

import com.hrms.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SalaryRepository extends JpaRepository<Salary, Long>, JpaSpecificationExecutor<Salary> {

    boolean existsByEmployeeIdAndSalaryMonth(Long employeeId, String salaryMonth);

    java.util.List<Salary> findBySalaryMonth(String salaryMonth);

    @Query("select coalesce(sum(s.netSalary), 0) from Salary s where s.salaryMonth = :month")
    BigDecimal sumNetSalaryByMonth(@Param("month") String month);

    @Query("select s.salaryMonth, sum(s.netSalary) from Salary s " +
            "where s.salaryMonth >= :startMonth group by s.salaryMonth order by s.salaryMonth")
    List<Object[]> sumByMonthFrom(@Param("startMonth") String startMonth);
}
