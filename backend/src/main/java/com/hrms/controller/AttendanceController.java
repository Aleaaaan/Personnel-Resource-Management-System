package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Attendance;
import com.hrms.entity.SysUser;
import com.hrms.service.AttendanceService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ApiResponse<Page<Attendance>> page(@RequestParam(required = false) Long employeeId,
                                              @RequestParam(required = false) String month,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        Long targetId = isEmployee(user) ? user.getEmployeeId() : employeeId;
        return ApiResponse.ok(attendanceService.page(targetId, month, status, page, size));
    }

    @GetMapping("/today")
    public ApiResponse<Attendance> today(Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        return ApiResponse.ok(attendanceService.todayRecord(user.getEmployeeId()));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@RequestParam(required = false) String month,
                                                  @RequestParam(required = false) Long employeeId,
                                                  Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        Long targetId = isEmployee(user) ? user.getEmployeeId() : employeeId;
        return ApiResponse.ok(attendanceService.monthStats(targetId, month));
    }

    @PostMapping("/check-in")
    public ApiResponse<Attendance> checkIn(Authentication authentication) {
        return ApiResponse.ok(attendanceService.checkIn((SysUser) authentication.getPrincipal()));
    }

    @PostMapping("/check-out")
    public ApiResponse<Attendance> checkOut(Authentication authentication) {
        return ApiResponse.ok(attendanceService.checkOut((SysUser) authentication.getPrincipal()));
    }

    private boolean isEmployee(SysUser user) {
        return "EMPLOYEE".equals(user.getRole());
    }
}
