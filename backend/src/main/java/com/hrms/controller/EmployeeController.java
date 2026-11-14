package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Employee;
import com.hrms.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ApiResponse<Page<Employee>> page(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long departmentId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(employeeService.page(keyword, departmentId, status, page, size));
    }

    @GetMapping("/all")
    public ApiResponse<List<Employee>> all() {
        return ApiResponse.ok(employeeService.allActive());
    }

    @GetMapping("/{id}")
    public ApiResponse<Employee> get(@PathVariable Long id) {
        return ApiResponse.ok(employeeService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Employee> create(@RequestBody Employee employee) {
        return ApiResponse.ok(employeeService.create(employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Employee> update(@PathVariable Long id, @RequestBody Employee employee) {
        return ApiResponse.ok(employeeService.update(id, employee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.ok();
    }
}
