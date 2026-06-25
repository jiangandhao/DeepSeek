package com.health.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.common.BizException;
import com.health.common.Result;
import com.health.entity.GlucoseRecord;
import com.health.entity.HealthDevice;
import com.health.entity.User;
import com.health.mapper.GlucoseRecordMapper;
import com.health.mapper.HealthDeviceMapper;
import com.health.mapper.UserMapper;
import com.health.security.UserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员运维")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Set<String> DEVICE_STATUSES = Set.of("ONLINE", "OFFLINE", "LOW_BATTERY", "UNBOUND");

    private final UserMapper userMapper;
    private final HealthDeviceMapper deviceMapper;
    private final GlucoseRecordMapper glucoseMapper;
    private final PasswordEncoder passwordEncoder;

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

    @Operation(summary = "用户管理列表(支持关键字搜索)")
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users(@RequestParam(required = false) String keyword) {
        requireAdmin();
        var query = Wrappers.<User>lambdaQuery().orderByDesc(User::getCreatedAt).last("limit 100");
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            query.and(w -> w.like(User::getUsername, kw).or().like(User::getNickname, kw));
        }
        List<User> users = userMapper.selectList(query);
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

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> setUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User admin = requireAdmin();
        User target = mustFindUser(id);
        if (target.getId().equals(admin.getId())) {
            throw new BizException(400, "不能禁用当前管理员账号");
        }
        int status = parseInt(body.get("status"), -1);
        if (status != 0 && status != 1) {
            throw new BizException(400, "status 仅支持 0(禁用) 或 1(启用)");
        }
        target.setStatus(status);
        userMapper.updateById(target);
        return Result.ok();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireAdmin();
        User target = mustFindUser(id);
        String password = body.get("password") == null ? "" : body.get("password").toString();
        if (password.length() < 6 || password.length() > 32) {
            throw new BizException(400, "密码长度需为 6-32 位");
        }
        target.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(target);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User admin = requireAdmin();
        User target = mustFindUser(id);
        if (target.getId().equals(admin.getId()) || "admin".equals(target.getUsername())) {
            throw new BizException(400, "不能删除管理员账号");
        }
        deviceMapper.delete(Wrappers.<HealthDevice>lambdaQuery().eq(HealthDevice::getUserId, id));
        userMapper.deleteById(id);
        return Result.ok();
    }

    @Operation(summary = "设备管理列表")
    @GetMapping("/devices")
    public Result<List<HealthDevice>> devices() {
        requireAdmin();
        return Result.ok(deviceMapper.selectList(Wrappers.<HealthDevice>lambdaQuery()
                .orderByDesc(HealthDevice::getUpdatedAt)
                .last("limit 100")));
    }

    @Operation(summary = "更新设备状态")
    @PutMapping("/devices/{id}/status")
    public Result<Void> setDeviceStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        requireAdmin();
        HealthDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BizException(404, "设备不存在");
        }
        String status = body.get("status") == null ? "" : body.get("status").toString();
        if (!DEVICE_STATUSES.contains(status)) {
            throw new BizException(400, "非法的设备状态");
        }
        device.setStatus(status);
        deviceMapper.updateById(device);
        return Result.ok();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/devices/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        requireAdmin();
        if (deviceMapper.selectById(id) == null) {
            throw new BizException(404, "设备不存在");
        }
        deviceMapper.deleteById(id);
        return Result.ok();
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

    private User requireAdmin() {
        User user = userMapper.selectById(UserContext.currentUserId());
        if (user == null || !"admin".equals(user.getUsername())) {
            throw new BizException(403, "仅管理员账号可访问运维端");
        }
        return user;
    }

    private User mustFindUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    private int parseInt(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
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
