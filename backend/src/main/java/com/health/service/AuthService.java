package com.health.service;

import com.health.entity.User;
import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password);
    Map<String, Object> register(User user);
    User getCurrentUser(Long userId);
}
