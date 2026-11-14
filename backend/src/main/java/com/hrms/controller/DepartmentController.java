package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Department;
import com.hrms.service.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public ApiResponse<List<Department>> list() {
        return ApiResponse.ok(departmentService.list());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Department> create(@RequestBody Department department) {
        return ApiResponse.ok(departmentService.create(department));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Department> update(@PathVariable Long id, @RequestBody Department department) {
        return ApiResponse.ok(departmentService.update(id, department));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok();
    }
}
