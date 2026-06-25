package com.health.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.common.BizException;
import com.health.dto.DeviceBindRequest;
import com.health.dto.DeviceTelemetryRequest;
import com.health.dto.GlucoseRecordRequest;
import com.health.entity.HealthDevice;
import com.health.mapper.HealthDeviceMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final HealthDeviceMapper deviceMapper;
    private final GlucoseService glucoseService;
    private final ObjectMapper objectMapper;

    public List<HealthDevice> list(Long userId) {
        return deviceMapper.selectList(Wrappers.<HealthDevice>lambdaQuery()
                .eq(HealthDevice::getUserId, userId)
                .orderByDesc(HealthDevice::getBoundAt));
    }

    public HealthDevice bind(Long userId, DeviceBindRequest req) {
        HealthDevice exists = deviceMapper.selectOne(Wrappers.<HealthDevice>lambdaQuery()
                .eq(HealthDevice::getUserId, userId)
                .eq(HealthDevice::getDeviceNo, req.getDeviceNo()));
        if (exists != null) {
            exists.setDeviceName(defaultText(req.getDeviceName(), exists.getDeviceName()));
            exists.setDeviceType(defaultText(req.getDeviceType(), exists.getDeviceType()));
            exists.setStatus("ONLINE");
            exists.setUpdatedAt(LocalDateTime.now());
            deviceMapper.updateById(exists);
            return exists;
        }

        HealthDevice device = new HealthDevice();
        device.setUserId(userId);
        device.setDeviceNo(req.getDeviceNo());
        device.setDeviceName(defaultText(req.getDeviceName(), "虚拟连续血糖仪"));
        device.setDeviceType(defaultText(req.getDeviceType(), "CGM"));
        device.setStatus("ONLINE");
        device.setBatteryLevel(92);
        device.setSignalStrength(88);
        device.setBoundAt(LocalDateTime.now());
        deviceMapper.insert(device);
        return device;
    }

    public HealthDevice simulate(Long userId, Long id, DeviceTelemetryRequest req) {
        HealthDevice device = deviceMapper.selectById(id);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BizException("设备不存在或未绑定");
        }
        LocalDateTime measuredAt = req.getMeasuredAt() != null ? req.getMeasuredAt() : LocalDateTime.now();
        String period = defaultText(req.getPeriod(), "RANDOM");

        GlucoseRecordRequest glucose = new GlucoseRecordRequest();
        glucose.setValueMmol(req.getValueMmol());
        glucose.setPeriod(period);
        glucose.setMeasuredAt(measuredAt);
        glucose.setNote("来自设备 " + device.getDeviceName() + "(" + device.getDeviceNo() + ") 的模拟上报");
        glucoseService.add(userId, glucose);

        device.setStatus(resolveStatus(req.getBatteryLevel()));
        device.setBatteryLevel(req.getBatteryLevel() != null ? req.getBatteryLevel() : device.getBatteryLevel());
        device.setSignalStrength(req.getSignalStrength() != null ? req.getSignalStrength() : device.getSignalStrength());
        device.setLastValueMmol(req.getValueMmol());
        device.setLastMeasuredAt(measuredAt);
        device.setLastPayload(toPayload(req, period, measuredAt));
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    public HealthDevice unbind(Long userId, Long id) {
        HealthDevice device = deviceMapper.selectById(id);
        if (device == null || !device.getUserId().equals(userId)) {
            throw new BizException("设备不存在或未绑定");
        }
        device.setStatus("UNBOUND");
        device.setUpdatedAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    private String resolveStatus(Integer battery) {
        if (battery != null && battery < 20) {
            return "LOW_BATTERY";
        }
        return "ONLINE";
    }

    private String toPayload(DeviceTelemetryRequest req, String period, LocalDateTime measuredAt) {
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("valueMmol", req.getValueMmol());
            payload.put("period", period);
            payload.put("batteryLevel", req.getBatteryLevel());
            payload.put("signalStrength", req.getSignalStrength());
            payload.put("measuredAt", measuredAt.toString());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
