package com.health.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.Result;
import com.health.entity.GlucoseRecord;
import com.health.entity.HealthDevice;
import com.health.entity.User;
import com.health.mapper.GlucoseRecordMapper;
import com.health.mapper.HealthDeviceMapper;
import com.health.mapper.UserMapper;
import com.health.common.BizException;
import com.health.security.UserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员运维")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final HealthDeviceMapper deviceMapper;
    private final GlucoseRecordMapper glucoseMapper;

    @Operation(summary = "运维总览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        requireAdmin();
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getStatus, 1));
        long devices = deviceMapper.selectCount(null);
        long onlineDevices = deviceMapper.selectCount(Wrappers.<HealthDevice>lambdaQuery()
                .eq(HealthDevice::getStatus, "ONLINE"));
        long todayActive = activeUsersBetween(LocalDate.now(), LocalDate.now().plusDays(1));

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("activeUsers", activeUsers);
        data.put("todayActive", todayActive);
        data.put("devices", devices);
        data.put("onlineDevices", onlineDevices);
        data.put("dailyActive", dailyActive());
        return Result.ok(data);
    }

    @Operation(summary = "用户管理列表")
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users() {
        requireAdmin();
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery().orderByDesc(User::getCreatedAt).last("limit 50"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", user.getId());
            row.put("username", user.getUsername());
            row.put("nickname", user.getNickname());
            row.put("gender", user.getGender());
            row.put("status", user.getStatus());
            row.put("createdAt", user.getCreatedAt());
            row.put("deviceCount", deviceMapper.selectCount(Wrappers.<HealthDevice>lambdaQuery().eq(HealthDevice::getUserId, user.getId())));
            rows.add(row);
        }
        return Result.ok(rows);
    }

    @Operation(summary = "设备管理列表")
    @GetMapping("/devices")
    public Result<List<HealthDevice>> devices() {
        requireAdmin();
        return Result.ok(deviceMapper.selectList(Wrappers.<HealthDevice>lambdaQuery()
                .orderByDesc(HealthDevice::getUpdatedAt)
                .last("limit 100")));
    }

    private List<Map<String, Object>> dailyActive() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = activeUsersBetween(day, day.plusDays(1));
            Map<String, Object> row = new HashMap<>();
            row.put("date", day.toString());
            row.put("activeUsers", count);
            rows.add(row);
        }
        return rows;
    }

    private void requireAdmin() {
        User user = userMapper.selectById(UserContext.currentUserId());
        if (user == null || !"admin".equals(user.getUsername())) {
            throw new BizException(403, "仅管理员账号可访问运维端");
        }
    }

    private long activeUsersBetween(LocalDate from, LocalDate to) {
        return glucoseMapper.selectList(Wrappers.<GlucoseRecord>lambdaQuery()
                .ge(GlucoseRecord::getMeasuredAt, from.atStartOfDay())
                .lt(GlucoseRecord::getMeasuredAt, to.atStartOfDay()))
                .stream()
                .map(GlucoseRecord::getUserId)
                .distinct()
                .count();
    }
}
