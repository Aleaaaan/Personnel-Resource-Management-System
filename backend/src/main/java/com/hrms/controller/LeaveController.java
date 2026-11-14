package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.LeaveApprovalRequest;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.SysUser;
import com.hrms.service.LeaveService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ApiResponse<Page<LeaveRequest>> page(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long employeeId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        Long targetId = "EMPLOYEE".equals(user.getRole()) ? user.getEmployeeId() : employeeId;
        return ApiResponse.ok(leaveService.page(targetId, status, page, size));
    }

    @PostMapping
    public ApiResponse<LeaveRequest> create(@RequestBody LeaveRequest request, Authentication authentication) {
        return ApiResponse.ok(leaveService.create((SysUser) authentication.getPrincipal(), request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<LeaveRequest> approve(@PathVariable Long id,
                                             @Valid @RequestBody LeaveApprovalRequest request,
                                             Authentication authentication) {
        return ApiResponse.ok(leaveService.approve(id, request, (SysUser) authentication.getPrincipal()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        leaveService.delete(id, (SysUser) authentication.getPrincipal());
        return ApiResponse.ok();
    }
}
