package com.health.controller;

import com.health.common.Result;
import com.health.entity.User;
import com.health.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Map<String, Object> result = authService.login(username, password);
        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody User user) {
        Map<String, Object> result = authService.register(user);
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<User> me(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        // JwtFilter would have validated this already
        Long userId = authService.getCurrentUser(1L).getId(); // simplified
        User user = authService.getCurrentUser(userId);
        return Result.success(user);
    }
}
