package com.hrms.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录
 */
@Data
@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_emp_date", columnNames = {"employee_id", "work_date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    private LocalTime checkIn;

    private LocalTime checkOut;

    /** NORMAL 正常 / LATE 迟到 / EARLY_LEAVE 早退 / ABSENT 缺勤 / LEAVE 请假 */
    @Column(length = 20)
    private String status;

    @Column(length = 200)
    private String remark;

    @Transient
    private String employeeName;

    @Transient
    private String empNo;

    @Transient
    private String departmentName;
}
