package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.dto.LoginRequest;
import com.hrms.dto.LoginResponse;
import com.hrms.entity.SysUser;
import com.hrms.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        return ApiResponse.ok(new LoginResponse(null, user.getId(), user.getUsername(),
                user.getRealName(), user.getRole(), user.getEmployeeId()));
    }
}
