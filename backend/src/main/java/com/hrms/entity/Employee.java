package com.hrms.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工档案
 */
@Data
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String empNo;

    @Column(nullable = false, length = 50)
    private String name;

    /** 男 / 女 */
    @Column(length = 10)
    private String gender;

    private LocalDate birthday;

    @Column(length = 20)
    private String phone;

    @Column(length = 80)
    private String email;

    @Column(length = 30)
    private String idCard;

    private Long departmentId;

    private Long positionId;

    private LocalDate hireDate;

    /** ACTIVE 在职 / RESIGNED 离职 */
    @Column(length = 20)
    private String status;

    private BigDecimal baseSalary;

    @Column(length = 30)
    private String education;

    @Column(length = 200)
    private String address;

    private LocalDateTime createdAt;

    @Transient
    private String departmentName;

    @Transient
    private String positionName;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
