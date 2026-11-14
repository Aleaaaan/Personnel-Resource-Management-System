package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Position;
import com.hrms.service.PositionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping
    public ApiResponse<List<Position>> list() {
        return ApiResponse.ok(positionService.list());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Position> create(@RequestBody Position position) {
        return ApiResponse.ok(positionService.create(position));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Position> update(@PathVariable Long id, @RequestBody Position position) {
        return ApiResponse.ok(positionService.update(id, position));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ApiResponse.ok();
    }
}
