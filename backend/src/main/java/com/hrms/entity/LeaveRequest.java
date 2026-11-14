package com.hrms.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请
 */
@Data
@Entity
@Table(name = "leave_request")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    /** 年假 / 事假 / 病假 / 调休 */
    @Column(length = 20)
    private String type;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal days;

    @Column(length = 300)
    private String reason;

    /** PENDING 待审批 / APPROVED 已通过 / REJECTED 已驳回 */
    @Column(length = 20)
    private String status;

    @Column(length = 50)
    private String approverName;

    private LocalDateTime approveTime;

    @Column(length = 300)
    private String comment;

    private LocalDateTime createdAt;

    @Transient
    private String employeeName;

    @Transient
    private String empNo;

    @Transient
    private String departmentName;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
