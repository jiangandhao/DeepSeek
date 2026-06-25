package com.health.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.dto.LoginRequest;
import com.health.dto.LoginResponse;
import com.health.dto.RegisterRequest;
import com.health.entity.User;
import com.health.mapper.UserMapper;
import com.health.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse register(RegisterRequest req) {
        if ("admin".equalsIgnoreCase(req.getUsername())) {
            throw new BizException("管理员账号由系统初始化，请直接登录");
        }
        Long exists = userMapper.selectCount(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, req.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setStatus(1);
        userMapper.insert(user);
        return buildResponse(user);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被禁用");
        }
        return buildResponse(user);
    }

    private LoginResponse buildResponse(User user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }
}
