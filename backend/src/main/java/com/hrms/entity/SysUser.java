package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统用户（登录账号）
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String realName;

    /** ADMIN / HR / EMPLOYEE */
    @Column(nullable = false, length = 20)
    private String role;

    /** 关联员工档案ID，可为空 */
    private Long employeeId;

    /** 1 启用 0 禁用 */
    private Integer status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = 1;
        }
    }
}
