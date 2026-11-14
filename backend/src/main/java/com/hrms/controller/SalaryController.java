package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Salary;
import com.hrms.entity.SysUser;
import com.hrms.service.SalaryService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @GetMapping
    public ApiResponse<Page<Salary>> page(@RequestParam(required = false) Long employeeId,
                                          @RequestParam(required = false) String month,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        Long targetId = "EMPLOYEE".equals(user.getRole()) ? user.getEmployeeId() : employeeId;
        return ApiResponse.ok(salaryService.page(targetId, month, status, page, size));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Map<String, Object>> generate(@RequestParam String month) {
        return ApiResponse.ok(salaryService.generate(month));
    }

    @PostMapping("/pay-batch")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Map<String, Object>> payBatch(@RequestParam String month) {
        return ApiResponse.ok(salaryService.payBatch(month));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Salary> update(@PathVariable Long id, @RequestBody Salary salary) {
        return ApiResponse.ok(salaryService.update(id, salary));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Salary> pay(@PathVariable Long id) {
        return ApiResponse.ok(salaryService.pay(id));
    }
}
