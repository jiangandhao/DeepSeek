package com.health.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("health_device")
public class HealthDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String deviceNo;
    private String deviceName;
    private String deviceType;
    private String status;
    private Integer batteryLevel;
    private Integer signalStrength;
    private BigDecimal lastValueMmol;
    private LocalDateTime lastMeasuredAt;
    private String lastPayload;
    private LocalDateTime boundAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
