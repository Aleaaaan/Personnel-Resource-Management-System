package com.hrms.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 月度薪资
 */
@Data
@Entity
@Table(name = "salary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_salary_emp_month", columnNames = {"employee_id", "salary_month"})
})
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /** 薪资月份，格式 yyyy-MM */
    @Column(name = "salary_month", nullable = false, length = 10)
    private String salaryMonth;

    private BigDecimal baseSalary;

    private BigDecimal bonus;

    private BigDecimal allowance;

    private BigDecimal deduction;

    private BigDecimal socialSecurity;

    private BigDecimal tax;

    private BigDecimal netSalary;

    /** DRAFT 待发放 / PAID 已发放 */
    @Column(length = 20)
    private String status;

    private LocalDate payDate;

    private LocalDateTime createdAt;

    @Transient
    private String employeeName;

    @Transient
    private String empNo;

    @Transient
    private String departmentName;
}
