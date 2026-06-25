package com.health.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.DeviceBindRequest;
import com.health.dto.DeviceTelemetryRequest;
import com.health.entity.HealthDevice;
import com.health.security.UserContext;
import com.health.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "智能设备")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService service;

    @Operation(summary = "查询当前用户绑定设备")
    @GetMapping
    public Result<List<HealthDevice>> list() {
        return Result.ok(service.list(UserContext.currentUserId()));
    }

    @Operation(summary = "绑定虚拟设备")
    @PostMapping("/bind")
    public Result<HealthDevice> bind(@Valid @RequestBody DeviceBindRequest req) {
        return Result.ok(service.bind(UserContext.currentUserId(), req));
    }

    @Operation(summary = "模拟设备上报接口数据")
    @PostMapping("/{id}/simulate")
    public Result<HealthDevice> simulate(@PathVariable Long id, @Valid @RequestBody DeviceTelemetryRequest req) {
        return Result.ok(service.simulate(UserContext.currentUserId(), id, req));
    }

    @Operation(summary = "解绑设备")
    @DeleteMapping("/{id}")
    public Result<HealthDevice> unbind(@PathVariable Long id) {
        return Result.ok(service.unbind(UserContext.currentUserId(), id));
    }
}
