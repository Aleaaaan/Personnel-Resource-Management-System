package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.dto.LoginRequest;
import com.hrms.dto.LoginResponse;
import com.hrms.entity.SysUser;
import com.hrms.repository.SysUserRepository;
import com.hrms.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "该账号已被禁用，请联系管理员");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getEmployeeId());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getEmployeeId());
    }
}
