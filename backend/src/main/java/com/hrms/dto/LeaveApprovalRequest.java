package com.hrms.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LeaveApprovalRequest {

    /** APPROVED / REJECTED */
    @NotBlank(message = "审批结果不能为空")
    private String status;

    private String comment;
}
